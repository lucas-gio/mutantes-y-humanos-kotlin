package com.gioia.mutantesyhumanoskotlin

import com.gioia.mutantesyhumanoskotlin.api.ApiRestController
import com.gioia.mutantesyhumanoskotlin.config.PropertiesReader
import com.gioia.mutantesyhumanoskotlin.config.Property
import com.gioia.mutantesyhumanoskotlin.config.di
import com.gioia.mutantesyhumanoskotlin.utils.Path.*
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Jetty
import org.http4k.server.asServer
import org.kodein.di.instance

fun main() {
    initializeApp()
}

private val apiRestController: ApiRestController by di.instance()

val app: HttpHandler = routes(
    MUTANT.value bind POST to { request: Request-> apiRestController.processMutantPost(request)},
    STATS.value bind Method.GET to {apiRestController.processStats()},
    INDEX.value bind Method.GET to {Response(OK).body("")},
)

/**
 * Inicializa el servidor y mapea rutas a recursos.
 */
fun initializeApp() {
    val serverPort = PropertiesReader.getProperty(Property.SERVER_PORT.value)?.toInt() ?: 5000

     app
    .asServer(Jetty(serverPort))
    .start()
}