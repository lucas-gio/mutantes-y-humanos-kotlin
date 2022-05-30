package com.gioia.services.mutant


import com.application.exceptions.MutantDetectedException
import spock.lang.Specification

/**
 * Pruebas de relacionadas a la implementación de MutantVerificationService.
 */
class MutantVerificationServiceTest extends Specification{
    MutantVerificationService mutantVerificationService = new MutantVerificationService()

    def "Se prueba el correcto funcionamiento del método de verificación de adn"(){
        given: "Un array de adns a verificar SIN mutantes"
        String[] adnArray = new String[12]
        adnArray[0] = "ATGCGA"
        adnArray[1] = "CAGTGC"
        adnArray[2] = "TTATTT"
        adnArray[3] = "AGACGG"
        adnArray[4] = "GCGTCA"
        adnArray[5] = "TCACTG"
        adnArray[6] = "TTTATT"
        adnArray[7] = "asdasd"
        adnArray[8] = "      "
        adnArray[9] = ""
        adnArray[10] = "ttttag"   // <------ No es mutante, porque considera sólo mayúsculas.
        adnArray[11] = "AAAagg"   // <------ No es mutante, porque considera sólo mayúsculas.

        when: "Se llama al método"
        mutantVerificationService.verify(adnArray)

        then: "Se verifica que no hay ningún adn correspondiente a mutantes"
        notThrown(MutantDetectedException)

        when: "Se envía un array de adns a verificar CON mutantes"
        adnArray = new String[6]
        adnArray[0] = "ATGCGA"
        adnArray[1] = "CAGTGC"
        adnArray[2] = "TTATGT"
        adnArray[3] = "AGAAGG"
        adnArray[4] = "CCCCTA"  // <------ Mutante
        adnArray[5] = "TCACTG"

        mutantVerificationService.verify(adnArray)

        then:"Se obtuvo la excepción MutantDetectedException ya que se encontraron mutantes"
        thrown(MutantDetectedException)
    }

    def "Se prueba el método isMutant(String[]) para verificar el circuito completo que tiene en cuenta la verificación de columnas, filas, y en forma oblícua"(){
        given:"Un array sin mutantes"
        String [] sinMutantes = new String[6]
        sinMutantes[0] = "ATGCGA"
        sinMutantes[1] = "CAGTGC"
        sinMutantes[2] = "TTATTT"
        sinMutantes[3] = "AGACGG"
        sinMutantes[4] = "GCGTCA"
        sinMutantes[5] = "TCACTG"

        and: "Otro con mutantes en los tres elementos"
        String[] conMutantes1 = new String[6]
                        // .--  Mutante
        conMutantes1[0] = "ATGCGA"
        conMutantes1[1] = "CAGTGC"
        conMutantes1[2] = "TTATGT"
        conMutantes1[3] = "AGAAGG"
        conMutantes1[4] = "CCCCTA"  //<----- Mutante
        conMutantes1[5] = "TCACTG"
                            // ^--  Mutante

        and: "Otro con mutantes sólo en horizontal"
        String [] conMutantes2 = new String[6]
        conMutantes2[0] = "ATGCGA"
        conMutantes2[1] = "CAGTGC"
        conMutantes2[2] = "GTTTTT"  //<----- Mutante
        conMutantes2[3] = "AGACGG"
        conMutantes2[4] = "GCGTCA"
        conMutantes2[5] = "TCACTG"

        and: "Otro con mutantes sólo en vertical"
        String [] conMutantes3 = new String[6]
        conMutantes3[0] = "AGGCGA"
        conMutantes3[1] = "CTGTGC"
        conMutantes3[2] = "TTATTT"
        conMutantes3[3] = "ATACGG"
        conMutantes3[4] = "GTGTCA"
        conMutantes3[5] = "TCACTG"
                         // ^--  Mutante

        and: "Otro con mutantes sólo en oblícuo"
        String [] conMutantes4 = new String[6]
        conMutantes4[0] = "ATGCGA"
        conMutantes4[1] = "CAGTGC"
        conMutantes4[2] = "TTATTT"
        conMutantes4[3] = "AGTCGG"
        conMutantes4[4] = "GCGTCA"
        conMutantes4[5] = "TCACTG"
                            // ^--  Mutante

        when: "Se evalúa con el que no tiene mutantes"
        boolean result1 = mutantVerificationService.isMutant(sinMutantes)

        then: "Se obtiene que no hay mutantes"
        result1 == false

        when: "Se evalúa con el que tiene mutantes en los tres elementos"
        boolean result2 = mutantVerificationService.isMutant(conMutantes1)
        then: "Se obtiene que hay mutantes"
        result2 == true

        when: "Se evalúa con el que tiene mutantes en forma horizontal"
        boolean result3 = mutantVerificationService.isMutant(conMutantes2)
        then: "Se obtiene que hay mutantes"
        result3 == true

        when: "Se evalúa con el que tiene mutantes en forma vertical"
        boolean result4 = mutantVerificationService.isMutant(conMutantes3)
        then: "Se obtiene que hay mutantes"
        result4 == true

        when: "Se evalúa con el que tiene mutantes en forma oblícua"
        boolean result5 = mutantVerificationService.isMutant(conMutantes4)
        then: "Se obtiene que hay mutantes"
        result5 == true
    }
}
