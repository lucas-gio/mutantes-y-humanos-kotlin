package com.gioia.mutantesyhumanoskotlin.api

import com.gioia.mutantesyhumanoskotlin.config.di
import com.gioia.mutantesyhumanoskotlin.domain.DnaReceived
import com.gioia.mutantesyhumanoskotlin.domain.Stat
import com.gioia.mutantesyhumanoskotlin.exceptions.RestMutantValidationException
import com.gioia.mutantesyhumanoskotlin.services.api.ApiService
import com.mongodb.BasicDBObject
import com.mongodb.client.MongoDatabase
import org.bson.Document
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.kodein.di.instance

/**
 * Pruebas de relacionadas a la implementación de ApiRestService.
 */
class ApiRestServiceTest{
    private val apiRestService: ApiService by di.instance()
    private val mongoDatabase: MongoDatabase by di.instance()

    @Test
    fun `Se verifica que al obtener un json con el adn el el cuerpo de un mensaje, se interprete correctamente como array`(){
        //when: "Se envía cuatro mensajes con adn válido"
        val mutant1 = apiRestService.parseReceivedDna("""{"dna":["ATGCGA","CAGTGC","TTATGT","AGAAGG","CCCCTA","TCACTG"]}""")
        val mutant2 = apiRestService.parseReceivedDna("""{"dna":["ATGCGA","CAGTGC","GTTTTT","AGACGG","GCGTCA","TCACTG"]}""")
        val mutant3 = apiRestService.parseReceivedDna("""{"dna":["AGGCGA","CTGTGC","TTATTT","ATACGG","GTGTCA","TCACTG"]}""")
        val mutant4 = apiRestService.parseReceivedDna("""{"dna":["ATGCGA","CAGTGC","TTATTT","AGTCGG","GCGTCA","TCACTG"]}""")

        //then: "Cada uno de ellos se interpreta como array correctamente"
        assertEquals(mutant1[0] , "ATGCGA")
        assertEquals(mutant1[1] , "CAGTGC")
        assertEquals(mutant1[2] , "TTATGT")
        assertEquals(mutant1[3] , "AGAAGG")
        assertEquals(mutant1[4] , "CCCCTA")
        assertEquals(mutant1[5] , "TCACTG")

        assertEquals(mutant2[0] , "ATGCGA")
        assertEquals(mutant2[1] , "CAGTGC")
        assertEquals(mutant2[2] , "GTTTTT")
        assertEquals(mutant2[3] , "AGACGG")
        assertEquals(mutant2[4] , "GCGTCA")
        assertEquals(mutant2[5] , "TCACTG")

        assertEquals(mutant3[0] , "AGGCGA")
        assertEquals(mutant3[1] , "CTGTGC")
        assertEquals(mutant3[2] , "TTATTT")
        assertEquals(mutant3[3] , "ATACGG")
        assertEquals(mutant3[4] , "GTGTCA")
        assertEquals(mutant3[5] , "TCACTG")

        assertEquals(mutant4[0] , "ATGCGA")
        assertEquals(mutant4[1] , "CAGTGC")
        assertEquals(mutant4[2] , "TTATTT")
        assertEquals(mutant4[3] , "AGTCGG")
        assertEquals(mutant4[4] , "GCGTCA")
        assertEquals(mutant4[5] , "TCACTG")
    }

    @Test
    fun `Se prueba que se almacenen correctamente los adn con los datos requeridos para su guardado`(){
        //given: "Una colección de adn vacía"
        cleanAllDnaCollection()

        //and : "Dos adn recibidos"
        //when:"Se tratan de almacenar"
        apiRestService.saveDnaReceived(arrayOf(
            "ATGCGA",
            "CAGTGC",
            "TTATTT",
            "AGACGG",
            "GCGTCA",
            "TCACTG"
        ), false)
        apiRestService.saveDnaReceived(arrayOf(
            "ATGCGA",
            "CAGTGC",
            "GTTTTT" , //<----- Mutante
            "AGACGG",
            "GCGTCA",
            "TCACTG",
        ), true)

        val storedDnas = mutableListOf<Document>()

        mongoDatabase
            .getCollection(DnaReceived.collectionName)
            .find(BasicDBObject())
            .toCollection(storedDnas)

        //then: "Existen en la bd"
        assertNotNull(storedDnas)
        assertEquals(storedDnas.size , 2)

        //and: "Tiene un id, y un adn con 6 elementos"
        val human = storedDnas.find{!it.getBoolean("isMutant")}
        assertNotNull(human)
        assertNotNull(human?.get("_id"))
        assertNotNull(human?.get("dna"))
        assertEquals((human?.get("dna") as List<*>).size , 6)
        (human["dna"] as List<*>)
            .containsAll(
                listOf(
                    "ATGCGA",
                    "CAGTGC",
                    "TTATTT",
                    "AGACGG",
                    "GCGTCA",
                    "TCACTG"
                )
            )

        val mutant = storedDnas.find{it.getBoolean("isMutant")}
        assertNotNull(mutant?.get("_id"))
        assertNotNull(mutant?.get("dna"))
        assertEquals((mutant?.get("dna") as List<*>).size , 6)
        (mutant["dna"] as List<*>)
            .containsAll(
                listOf(
                    "ATGCGA",
                    "CAGTGC",
                    "GTTTTT",
                    "AGACGG",
                    "GCGTCA",
                    "TCACTG"
                )
            )

        //cleanup: "Se eliminan los datos generados"
        cleanAllDnaCollection()
    }
    @Test
    fun `Se verifica que ante un ingreso de valores inválidos de adn falle el validador`(){
        //when: "Se valida un adn null"
        //then: "Se obtiene una excepción de validación"
        assertThrows(RestMutantValidationException::class.java) {
            apiRestService.validateDnaReceived(null)
        }

        //when: "Se valida un adn vacío"
        //then: "Se obtiene una excepción de validación"
        assertThrows(RestMutantValidationException::class.java) {
            apiRestService.validateDnaReceived(arrayOf())
        }

        //when: "Se valida un adn con valor inválido"
        //then: "Se obtiene una excepción de validación"
        assertThrows(RestMutantValidationException::class.java) {
            apiRestService.validateDnaReceived(arrayOf(
                "XYZ",
                "CAGkGC",
                "tTATGT",
                "AGAAGG",
                "CCCCTA",
                "TCACTG"
            ))
        }
    }
    @Test
    fun `Se prueba que el método que almacena incrementos en stats funcione incrementando mutantes cuando recibe mutantes y humanos en caso contrario - Además de ser un solo registro`(){
        //given: "La colección de estadísticas vacías"
        cleanStats()

        //when: "Se envían 50 mutantes y 100 humanos"
        repeat(50){
            apiRestService.saveStat(true)
        }

        repeat(100){
            apiRestService.saveStat(false)
        }

        val statDocuments = mutableListOf<Document>()
        mongoDatabase
            .getCollection(Stat.collectionName)
            .find()
            .toCollection(statDocuments)

        //then: "Es sólo un registro"
        assertNotNull(statDocuments)
        assertEquals(statDocuments.size , 1)

        //and: "Tiene los valores esperados"
        assertEquals(statDocuments.first()[Stat.fieldMutantsQuantity], 50)
        assertEquals(statDocuments.first()[Stat.fieldHumansQuantity], 100)

        //cleanup: "Se elimina la colección de estadísticas utilizada."
        cleanStats()
    }

    /**
     * Borra toda la colección dna received
     */
    private fun cleanAllDnaCollection(){
        mongoDatabase.getCollection(DnaReceived.collectionName).deleteMany(BasicDBObject())
    }

    /**
     * Borra toda la colección stats.
     */
    private fun cleanStats(){
        mongoDatabase.getCollection(Stat.collectionName).deleteMany(BasicDBObject())
    }
}
