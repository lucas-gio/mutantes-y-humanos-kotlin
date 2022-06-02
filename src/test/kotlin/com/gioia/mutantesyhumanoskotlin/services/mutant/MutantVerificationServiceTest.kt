package com.gioia.mutantesyhumanoskotlin.services.mutant


import com.gioia.mutantesyhumanoskotlin.config.di
import com.gioia.mutantesyhumanoskotlin.exceptions.MutantDetectedException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.kodein.di.instance

/**
 * Pruebas de relacionadas a la implementación de MutantVerificationService.
 */
class MutantVerificationServiceTest{
    private val mutantService: MutantService by di.instance()

    @Test
    fun `Se prueba el correcto funcionamiento del método de verificación de adn`(){
        //given: "Un array de adns a verificar SIN mutantes"
        //when: "Se llama al método"
        //then: "Se verifica que no hay ningún adn correspondiente a mutantes"
        Assertions.assertDoesNotThrow {
            (mutantService as MutantVerificationService).verify(
                arrayOf(
                    "ATGCGA",
                    "CAGTGC",
                    "TTATTT",
                    "AGACGG",
                    "GCGTCA",
                    "TCACTG",
                    "TTTATT",
                    "asdasd",
                    "      ",
                    "",
                    "ttttag",   // <------ No es mutante, porque considera sólo mayúsculas.
                    "AAAagg"   // <------ No es mutante, porque considera sólo mayúsculas.
                )
            )
        }

        //when: "Se envía un array de adns a verificar CON mutantes"
        //then:"Se obtuvo la excepción MutantDetectedException ya que se encontraron mutantes"
        Assertions.assertThrows(MutantDetectedException::class.java){
            (mutantService as MutantVerificationService).verify(
                arrayOf(
                    "ATGCGA",
                    "CAGTGC",
                    "TTATGT",
                    "AGAAGG",
                    "CCCCTA", // <------ Mutante
                    "TCACTG",
                )
            )
        }
    }

    @Test
    fun `Se prueba el método isMutant() para verificar el circuito completo que tiene en cuenta la verificación de columnas, filas, y en forma oblícua`(){
        //given:"Un array sin mutantes"
        val sinMutantes = arrayOf(
            "ATGCGA",
            "CAGTGC",
            "TTATTT",
            "AGACGG",
            "GCGTCA",
            "TCACTG",
        )

        //and: "Otro con mutantes en los tres elementos"
        val conMutantes1 = arrayOf(
            // .--  Mutante
            "ATGCGA",
            "CAGTGC",
            "TTATGT",
            "AGAAGG",
            "CCCCTA", //<----- Mutante
            "TCACTG",
            // ^--  Mutante
        )

        //and: "Otro con mutantes sólo en horizontal"
        val conMutantes2 = arrayOf(
            "ATGCGA",
            "CAGTGC",
            "GTTTTT",  //<----- Mutante
            "AGACGG",
            "GCGTCA",
            "TCACTG"
        )

        //and: "Otro con mutantes sólo en vertical"
        val conMutantes3 = arrayOf(
            "AGGCGA",
            "CTGTGC",
            "TTATTT",
            "ATACGG",
            "GTGTCA",
            "TCACTG"
             // ^--  Mutante
        )

        //and: "Otro con mutantes sólo en oblícuo"
        val conMutantes4 = arrayOf(
            "ATGCGA",
            "CAGTGC",
            "TTATTT",
            "AGTCGG",
            "GCGTCA",
            "TCACTG"
              // ^--  Mutante
        )

        //when: "Se evalúa con el que no tiene mutantes : Se obtiene que no hay mutantes"
         Assertions.assertFalse(
             (mutantService as MutantVerificationService).isMutant(sinMutantes)
         )

        //when: "Se evalúa con el que tiene mutantes en los tres elementos: Se obtiene que hay mutantes"
        Assertions.assertTrue(
            (mutantService as MutantVerificationService).isMutant(conMutantes1)
        )

        //when: "Se evalúa con el que tiene mutantes en forma horizontal: Se obtiene que hay mutantes"
        Assertions.assertTrue(
            (mutantService as MutantVerificationService).isMutant(conMutantes2)
        )

        //when: "Se evalúa con el que tiene mutantes en forma vertical: Se obtiene que hay mutantes"
        Assertions.assertTrue(
            (mutantService as MutantVerificationService).isMutant(conMutantes3)
        )

        //when: "Se evalúa con el que tiene mutantes en forma oblícua: Se obtiene que hay mutantes"
        Assertions.assertTrue(
            (mutantService as MutantVerificationService).isMutant(conMutantes4)
        )
    }
}
