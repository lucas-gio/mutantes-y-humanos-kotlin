package com.gioia.api

import com.application.Application
import com.application.domain.DnaReceived
import com.application.domain.Stat
import com.application.services.mongo.AppMongoClient
import com.application.utils.Path
import com.mongodb.BasicDBObject
import spark.Spark
import spock.lang.Specification

/**
 * Pruebas de relacionadas a la implementación de ApiRestController.
 */
class ApiRestControllerTest extends Specification {
    def "Se verifica que al enviar un mutante se obtenga un estado 200, al enviar un humano un 403, al enviar un mensaje con errores de validación 422, y con errores de sintaxis 400. Al final, se obtienen las estadísticas"() {
        given: "El servidor levantado"
        java.lang.Thread thread1 = new Thread("Test server 1") {
            void run() {
                try {
                    Application.main(new String[0])
                }
                catch (InterruptedException) {
                    Spark.stop()
                    return
                }
            }
        }

        thread1.start()
        while (!Application.isLoaded) Thread.sleep(500)
        final String MUTANT_URL = "http://localhost:${ApiRestController.SERVER_PORT}${Path.MUTANT}"

        and: "La colección de adn vacía"
        cleanAllCollection()

        when: "Se realiza un post con un mutante"
        String message = '{"dna":["ATGCGA","CAGTGC","TTATGC","AGAAGG","CCCCTA","TCACTG"]}'
        HttpURLConnection post = doPost(MUTANT_URL, message)
        int responseCode = post.getResponseCode()

        then: "Se obtiene un 200"
        responseCode == HttpURLConnection.HTTP_OK

        when: "Se envía un humano"
        message = '{"dna":["ATGCGA","CAGTGC","TTATTT","AGACGG","GCGTCA","TCACTG"]}'
        post = doPost(MUTANT_URL, message)
        responseCode = post.getResponseCode()

        then: "Se obtiene un 403"
        responseCode == HttpURLConnection.HTTP_FORBIDDEN

        when: "Se envía un mensaje con error de validación (valores inválidos)"
        message = '{"dna":["XYZ","CAGTGC","2345","AGAAGG","CCCCTA","TCACTG"]}'
        post = doPost(MUTANT_URL, message)
        responseCode = post.getResponseCode()

        then: "Se obtiene un 422"
        responseCode == 422

        when: "Se envía otro mensaje con error de validación (valor null)"
        message = '{"dna":["ATGCGA","CAGTGC","TTATGT","AGAAGG","CCCCTA",,"TCACTG"]}'
        post = doPost(MUTANT_URL, message)
        responseCode = post.getResponseCode()

        then: "Se obtiene un 422"
        responseCode == 422

        when: "Se envía un mensaje con error de sintaxis json (falta corchete de cierre)"
        message = '{"dna":["XYZ","CAGTGC","2345","AGAAGG","CCCCTA"}'
        post = doPost(MUTANT_URL, message)
        responseCode = post.getResponseCode()

        then: "Se obtiene un 400"
        responseCode == HttpURLConnection.HTTP_BAD_REQUEST

        when: "Se cargan 3 mutantes y 9 humanos más"
        message = '{"dna":["ATGCGA","CAGTGC","TTATGC","AGAAGG","CCCCTA","TCACTG"]}'
        doPost(MUTANT_URL, message).getResponseCode()
        doPost(MUTANT_URL, message).getResponseCode()
        doPost(MUTANT_URL, message).getResponseCode()

        message = '{"dna":["ATGCGA","CAGTGC","TTATTT","AGACGG","GCGTCA","TCACTG"]}'
        doPost(MUTANT_URL, message).getResponseCode()
        doPost(MUTANT_URL, message).getResponseCode()
        doPost(MUTANT_URL, message).getResponseCode()
        doPost(MUTANT_URL, message).getResponseCode()
        doPost(MUTANT_URL, message).getResponseCode()
        doPost(MUTANT_URL, message).getResponseCode()
        doPost(MUTANT_URL, message).getResponseCode()
        doPost(MUTANT_URL, message).getResponseCode()
        doPost(MUTANT_URL, message).getResponseCode()

        and :"Se verifican las estadísticas"
        String jsonResponse = new URL("http://localhost:${ApiRestController.SERVER_PORT}${Path.STATS}").getText()

        then: "Se obtiene que hubo 4 mutantes y 10 humanos, con su ratio"
        jsonResponse == '{"count_mutant_dna":4,"count_human_dna":10,"ratio":0.40}'

        cleanup: "Se finaliza el servidor. y se elimina lo creado."
        thread1.interrupt()
        cleanAllCollection()
    }

    /**
     * Extracción para hacer un post
     * @param url la url a realizar un post.
     * @param body el cuerpo del mensaje
     * @return Una conexión para verificar posteriormente
     */
    private HttpURLConnection doPost(String url, String body){
        java.net.HttpURLConnection post = new URL(url).openConnection()
        post.setRequestMethod("POST")
        post.setDoOutput(true)
        post.setRequestProperty("Content-Type", "application/json")
        post.getOutputStream().write(body.getBytes("UTF-8"))

        return post
    }

    /**
     * Borra toda la colección dna received
     */
    private void cleanAllCollection(){
        AppMongoClient.getDb().getCollection(DnaReceived.collectionName).deleteMany(new BasicDBObject())
        AppMongoClient.getDb().getCollection(Stat.collectionName).deleteMany(new BasicDBObject())
    }
}
