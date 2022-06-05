package com.gioia.mutantesyhumanoskotlin.api

import com.gioia.mutantesyhumanoskotlin.app
import com.gioia.mutantesyhumanoskotlin.config.di
import com.gioia.mutantesyhumanoskotlin.domain.DnaReceived
import com.gioia.mutantesyhumanoskotlin.domain.Stat
import com.gioia.mutantesyhumanoskotlin.utils.Path
import com.gioia.mutantesyhumanoskotlin.utils.Path.MUTANT
import com.mongodb.BasicDBObject
import com.mongodb.client.MongoDatabase
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Status.Companion.FORBIDDEN
import org.http4k.core.Status.Companion.OK
import org.junit.jupiter.api.Test
import org.kodein.di.instance
import kotlin.test.assertEquals


/**
 * Pruebas de relacionadas a la implementación de ApiRestController.
 * Llamados a la api y sus respuestas
 */
class ApiRestControllerTest {
     @Test
     fun `Se verifica que al enviar un mutante se obtenga un estado 200, al enviar un humano un 403, al enviar un mensaje con errores de validación 422, y con errores de sintaxis 400, Al final, se obtienen las estadísticas`(){
        //given: "La colección de adn vacía"
        cleanAllCollection()

        // when: "Se realiza un post con un mutante"
         // then: "Se obtiene un 200"
        var message = """{"dna":["ATGCGA","CAGTGC","TTATGC","AGAAGG","CCCCTA","TCACTG"]}"""
         assertEquals(
             app(org.http4k.core.Request(POST, MUTANT.value).body(message)),
             Response(OK)
         )


         // when: "Se envía un humano"
         // then: "Se obtiene un 403"
         message = """{"dna":["ATGCGA","CAGTGC","TTATTT","AGACGG","GCGTCA","TCACTG"]}"""
         assertEquals(
             app(org.http4k.core.Request(POST, MUTANT.value).body(message)),
             Response(FORBIDDEN)
         )

         //when: "Se envía un mensaje con error de validación (valores inválidos)"
         //then: "Se obtiene un 422"
         message = """{"dna":["XYZ","CAGTGC","2345","AGAAGG","CCCCTA","TCACTG"]}"""
         assertEquals(
             app(org.http4k.core.Request(POST, MUTANT.value).body(message)),
             Response(Status.UNPROCESSABLE_ENTITY)
         )


         //when: "Se envía otro mensaje con error de validación (valor null)"
         //then: "Se obtiene un 422"
         message = """{"dna":["ATGCGA","CAGTGC","TTATGT","AGAAGG","CCCCTA",,"TCACTG"]}"""
         assertEquals(
             app(org.http4k.core.Request(POST, MUTANT.value).body(message)),
             Response(Status.UNPROCESSABLE_ENTITY)
         )

         //when: "Se envía un mensaje con error de sintaxis json (falta corchete de cierre)"
         // then: "Se obtiene un 400"
         message = """{"dna":["XYZ","CAGTGC","2345","AGAAGG","CCCCTA"}"""
         assertEquals(
             app(org.http4k.core.Request(POST, MUTANT.value).body(message)),
             Response(Status.BAD_REQUEST)
         )

        //when: "Se cargan 3 mutantes y 9 humanos más"
        message = """{"dna":["ATGCGA","CAGTGC","TTATGC","AGAAGG","CCCCTA","TCACTG"]}"""
         repeat(3){
             app(org.http4k.core.Request(POST, MUTANT.value).body(message))
         }

        message = """{"dna":["ATGCGA","CAGTGC","TTATTT","AGACGG","GCGTCA","TCACTG"]}"""
         repeat(9){
             app(org.http4k.core.Request(POST, MUTANT.value).body(message))
         }

        //then :"Se verifican las estadísticas, y se obtiene que hubo 4 mutantes y 10 humanos, con su ratio"
         assertEquals(
             app(org.http4k.core.Request(GET, Path.STATS.value).body(message)),
             Response(OK)
                 .body(
                     """{"count_mutant_dna":4,"count_human_dna":10,"ratio":0.40}"""
                 )
         )

        cleanAllCollection()
    }

    /**
     * Borra toda la colección dna received
     */
    private fun cleanAllCollection(){
        val mongoDatabase : MongoDatabase by di.instance()
        mongoDatabase.getCollection(DnaReceived.collectionName).deleteMany(BasicDBObject())
        mongoDatabase.getCollection(Stat.collectionName).deleteMany(BasicDBObject())
    }
}
