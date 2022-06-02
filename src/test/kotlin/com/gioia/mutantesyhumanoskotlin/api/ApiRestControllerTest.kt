package com.gioia.mutantesyhumanoskotlin.api

import com.gioia.mutantesyhumanoskotlin.config.di
import com.gioia.mutantesyhumanoskotlin.domain.DnaReceived
import com.gioia.mutantesyhumanoskotlin.domain.Stat
import com.gioia.mutantesyhumanoskotlin.utils.Path
import com.mongodb.BasicDBObject
import com.mongodb.client.MongoDatabase
import okhttp3.MediaType
import org.junit.jupiter.api.Test
import org.kodein.di.instance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


/**
 * Pruebas de relacionadas a la implementación de ApiRestController.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ApiRestControllerTest {
    @Autowired
    private val mockMvc: MockMvc? = null
     @Test
     fun `Se verifica que al enviar un mutante se obtenga un estado 200, al enviar un humano un 403, al enviar un mensaje con errores de validación 422, y con errores de sintaxis 400, Al final, se obtienen las estadísticas`(){
        //given: "La colección de adn vacía"
        cleanAllCollection()

        // when: "Se realiza un post con un mutante"
        var message = """{"dna":["ATGCGA","CAGTGC","TTATGC","AGAAGG","CCCCTA","TCACTG"]}"""
         val performPost = mockMvc?.post(Path.MUTANT.toString()){
            contentType = MediaType.APPLICATION_JSON,
             content = message
         }
         // then: "Se obtiene un 200"
         performPost
             .andExpect(status().isOk)
             .andExpect(content().contentType(MediaType.APPLICATION_JSON))

         // when: "Se envía un humano"
         message = """{"dna":["ATGCGA","CAGTGC","TTATTT","AGACGG","GCGTCA","TCACTG"]}"""
         val performPost = mockMvc.post(Path.MUTANT){
             contentType = MediaType.APPLICATION_JSON,
             content = message
         }
         // then: "Se obtiene un 403"
         performPost
             .andExpect(status().isForbidden)
             .andExpect(content().contentType(MediaType.APPLICATION_JSON))

         //when: "Se envía un mensaje con error de validación (valores inválidos)"
         message = """{"dna":["XYZ","CAGTGC","2345","AGAAGG","CCCCTA","TCACTG"]}"""
         val performPost = mockMvc.post(Path.MUTANT){
             contentType = MediaType.APPLICATION_JSON,
             content = message
         }
         //then: "Se obtiene un 422"
         performPost
             .andExpect(status().isUnprocessableEntity)
             .andExpect(content().contentType(MediaType.APPLICATION_JSON))

         //when: "Se envía otro mensaje con error de validación (valor null)"
         message = '{"dna":["ATGCGA","CAGTGC","TTATGT","AGAAGG","CCCCTA",,"TCACTG"]}'
         val performPost = mockMvc.post(Path.MUTANT){
             contentType = MediaType.APPLICATION_JSON,
             content = message
         }
         //then: "Se obtiene un 422"
         performPost
             .andExpect(status().isUnprocessableEntity)
             .andExpect(content().contentType(MediaType.APPLICATION_JSON))

         //when: "Se envía un mensaje con error de sintaxis json (falta corchete de cierre)"
         message = """{"dna":["XYZ","CAGTGC","2345","AGAAGG","CCCCTA"}"""
         val performPost = mockMvc.post(Path.MUTANT){
             contentType = MediaType.APPLICATION_JSON,
             content = message
         }
         // then: "Se obtiene un 400"
         performPost
             .andExpect(status().isBadRequest)
             .andExpect(content().contentType(MediaType.APPLICATION_JSON))

        //when: "Se cargan 3 mutantes y 9 humanos más"
        message = """{"dna":["ATGCGA","CAGTGC","TTATGC","AGAAGG","CCCCTA","TCACTG"]}"""
         val performPost = mockMvc.post(Path.MUTANT) {
             contentType = MediaType.APPLICATION_JSON,
             content = message
         }
         3.times{
             performPost
         }

        message = """{"dna":["ATGCGA","CAGTGC","TTATTT","AGACGG","GCGTCA","TCACTG"]}"""
         val performPost = mockMvc.post(Path.MUTANT) {
             contentType = MediaType.APPLICATION_JSON,
             content = message
         }
         9.times{
             performPost
         }

        //then :"Se verifican las estadísticas, y se obtiene que hubo 4 mutantes y 10 humanos, con su ratio"
         mockMvc
             .get(Path.STATS)
             .andExpect{
                 content{
                     """{"count_mutant_dna":4,"count_human_dna":10,"ratio":0.40}"""
                 }
             }

        cleanAllCollection()
    }

    /**
     * Borra toda la colección dna received
     */
    private fun cleanAllCollection(){
        val mongoDatabase : MongoDatabase by di.instance()
        mongoDatabase.getCollection(DnaReceived.collectionName).deleteMany(BasicDBObject())
        mongoDatabase.getCollection(Stat.collectionName).deleteMany(BasicDBObject())
    }*/
}
