package com.gioia.services.api

import com.application.domain.DnaReceived
import com.application.domain.Stat
import com.application.exceptions.RestMutantValidationException
import com.application.services.mongo.AppMongoClient
import com.mongodb.BasicDBObject
import org.bson.Document
import spock.lang.Specification

/**
 * Pruebas de relacionadas a la implementación de ApiRestService.
 */
class ApiRestServiceTest extends Specification{
    ApiRestService apiRestService = new ApiRestService()

    def "Se verifica que al obtener un json con el adn el el cuerpo de un mensaje, se interprete correctamente como array"(){
        when: "Se envía cuatro mensajes con adn válido"
        String[] mutant1 = apiRestService.parseReceivedDna('{"dna":["ATGCGA","CAGTGC","TTATGT","AGAAGG","CCCCTA","TCACTG"]}')
        String[] mutant2 = apiRestService.parseReceivedDna('{"dna":["ATGCGA","CAGTGC","GTTTTT","AGACGG","GCGTCA","TCACTG"]}')
        String[] mutant3 = apiRestService.parseReceivedDna('{"dna":["AGGCGA","CTGTGC","TTATTT","ATACGG","GTGTCA","TCACTG"]}')
        String[] mutant4 = apiRestService.parseReceivedDna('{"dna":["ATGCGA","CAGTGC","TTATTT","AGTCGG","GCGTCA","TCACTG"]}')

        then: "Cada uno de ellos se interpreta como array correctamente"
        mutant1[0] == "ATGCGA"
        mutant1[1] == "CAGTGC"
        mutant1[2] == "TTATGT"
        mutant1[3] == "AGAAGG"
        mutant1[4] == "CCCCTA"
        mutant1[5] == "TCACTG"

        mutant2[0] == "ATGCGA"
        mutant2[1] == "CAGTGC"
        mutant2[2] == "GTTTTT"
        mutant2[3] == "AGACGG"
        mutant2[4] == "GCGTCA"
        mutant2[5] == "TCACTG"

        mutant3[0] == "AGGCGA"
        mutant3[1] == "CTGTGC"
        mutant3[2] == "TTATTT"
        mutant3[3] == "ATACGG"
        mutant3[4] == "GTGTCA"
        mutant3[5] == "TCACTG"

        mutant4[0] == "ATGCGA"
        mutant4[1] == "CAGTGC"
        mutant4[2] == "TTATTT"
        mutant4[3] == "AGTCGG"
        mutant4[4] == "GCGTCA"
        mutant4[5] == "TCACTG"
    }

    def "Se prueba que se almacenen correctamente los adn con los datos requeridos para su guardado"(){
        given: "Una colección de adn vacía"
        cleanAllDnaCollection()

        and : "Dos adn recibidos"
        String[] human = new String[6]
        human[0] = "ATGCGA"
        human[1] = "CAGTGC"
        human[2] = "TTATTT"
        human[3] = "AGACGG"
        human[4] = "GCGTCA"
        human[5] = "TCACTG"

        String [] mutant = new String[6]
        mutant[0] = "ATGCGA"
        mutant[1] = "CAGTGC"
        mutant[2] = "GTTTTT"  //<----- Mutante
        mutant[3] = "AGACGG"
        mutant[4] = "GCGTCA"
        mutant[5] = "TCACTG"

        when:"Se tratan de almacenar"
        apiRestService.saveDnaReceived(human, false)
        apiRestService.saveDnaReceived(mutant, true)
        List<Document> storedDnas = AppMongoClient.getDb().getCollection(DnaReceived.collectionName).find(new BasicDBObject()).collect()

        then: "Existen en la bd"
        storedDnas != null
        storedDnas.size() == 2

        and: "Tiene un id, y un adn con 6 elementos"
        storedDnas.find{Document it-> it.isMutant == false}._id != null
        storedDnas.find{Document it-> it.isMutant == false}.dna != null
        storedDnas.find{Document it-> it.isMutant == false}.dna.size() == 6
        storedDnas.find{Document it-> it.isMutant == false}.dna.containsAll( [
                                "ATGCGA",
                                "CAGTGC",
                                "TTATTT",
                                "AGACGG",
                                "GCGTCA",
                                "TCACTG"
                ] )

        storedDnas.find{Document it-> it.isMutant == true}._id != null
        storedDnas.find{Document it-> it.isMutant == true}.dna != null
        storedDnas.find{Document it-> it.isMutant == true}.dna.size() == 6
        storedDnas.find{Document it-> it.isMutant == true}.dna.containsAll([
                        "ATGCGA",
                        "CAGTGC",
                        "GTTTTT",
                        "AGACGG",
                        "GCGTCA",
                        "TCACTG"
                ])

        cleanup: "Se eliminan los datos generados"
        cleanAllDnaCollection()
    }

    def "Se verifica que el método validInputRegex() retorne la expresión regular esperada"(){
        expect:"Al llamar al método se obtiene el valor esperado"
        apiRestService.validInputRegex() == '^([ATCG]{1,})$'
    }

    def "Se verifica que ante un ingreso de valores inválidos de adn falle el validador"(){
        when: "Se valida un adn null"
        String [] adn = null
        apiRestService.validateDnaReceived(adn)

        then: "Se obtiene una excepción de validación"
        thrown(RestMutantValidationException)

        when: "Se valida un adn vacío"
        adn = new String[0]
        apiRestService.validateDnaReceived(adn)

        then: "Se obtiene una excepción de validación"
        thrown(RestMutantValidationException)

        when: "Se valida un adn con valor inválido"
        adn = new String[6]
        adn[0] = "XYZ"
        adn[1] = "CAGkGC"
        adn[2] = "tTATGT"
        adn[3] = "AGAAGG"
        adn[4] = "CCCCTA"
        adn[5] = "TCACTG"

        apiRestService.validateDnaReceived(adn)

        then: "Se obtiene una excepción de validación"
        thrown(RestMutantValidationException)
    }

    def "Se prueba que el método que almacena incrementos en stats funcione incrementando mutantes cuando recibe mutantes, y humanos en caso contrario; Además de ser un solo registro"(){
        given: "La colección de estadísticas vacías"
        cleanStats();

        when: "Se envían 50 mutantes y 100 humanos"
        50.times{
            apiRestService.saveStat(true);
        }

        100.times {
            apiRestService.saveStat(false);
        }

        List<Document> statDocuments = AppMongoClient.getDb().getCollection(Stat.collectionName).find().collect();

        then: "Es sólo un registro"
        statDocuments
        statDocuments.size() == 1

        and: "Tiene los valores esperados"
        (statDocuments.first() as Document).get(Stat._mutantsQuantity) == 50
        (statDocuments.first() as Document).get(Stat._humansQuantity) == 100

        cleanup: "Se elimina la colección de estadísticas utilizada."
        cleanStats();
    }

    /**
     * Borra toda la colección dna received
     */
    private void cleanAllDnaCollection(){
        AppMongoClient.getDb().getCollection(DnaReceived.collectionName).deleteMany(new BasicDBObject())
    }

    /**
     * Borra toda la colección stats.
     */
    private void cleanStats(){
        AppMongoClient.getDb().getCollection(Stat.collectionName).deleteMany(new BasicDBObject());
    }
}
