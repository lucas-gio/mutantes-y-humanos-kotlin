package com.gioia.mutantesyhumanoskotlin.api

import com.gioia.mutantesyhumanoskotlin.exceptions.RestMutantValidationException
import com.gioia.mutantesyhumanoskotlin.services.api.ApiService
import com.gioia.mutantesyhumanoskotlin.services.mutant.MutantService
import com.gioia.mutantesyhumanoskotlin.services.stats.StatsService
import com.gioia.mutantesyhumanoskotlin.utils.Path
import com.google.gson.JsonSyntaxException
import org.slf4j.LoggerFactory
import spark.Request
import spark.Response
import spark.Spark
import java.net.HttpURLConnection

/**
 * Controlador dedicado a los ingresos al sistema por api rest.
 */
class ApiRestController(
	private val apiService: ApiService,
	private val mutantService: MutantService,
	private val statsService: StatsService
) {
	init {
		initializeRoutes()
	}

	/**
	 * Inicializa el servidor y mapea rutas a recursos.
	 */
	private fun initializeRoutes() {
		Spark.port(serverPort)
		val maxThreads = 8 //fixme variable de entorno
		val minThreads = 2//fixme variable de entorno
		val idleTimeoutMs = 30000//fixme variable de entorno
		Spark.threadPool(maxThreads, minThreads, idleTimeoutMs)
		Spark.staticFiles.location("/public") // Para loader.io
		Spark.post(
			Path.MUTANT.toString()
		) { request: Request, response: Response -> processMutantPost(request, response) }
		Spark.get(
			Path.STATS.toString()
		) { request: Request, response: Response -> processStats(request, response) }

		// Este caso específico es para el monitoreo de amazon beanstalk.
		Spark.get(Path.INDEX.toString()) { _, response: Response ->
			response.status(HttpURLConnection.HTTP_OK)
			""
		}
	}

	/**
	 * Procesa el ingreso de adn para verificar si es un humano o mutante.
	 * En caso de ser mutante o humano almacena un incremento para visualizarlo
	 * posteriormente como estadística.
	 * @param request El pedido del cliente.
	 * @param response La respuesta.
	 * @return Un cuerpo vacío y un estado http acorde al resultado obtenido.
	 */
	private fun processMutantPost(request: Request, response: Response): String {
		try {
			val dnasArray: Array<String> = apiService.parseReceivedDna(request.body())
			apiService.validateDnaReceived(dnasArray)
			val isMutant: Boolean = mutantService.isMutant(dnasArray)
			apiService.saveDnaReceived(dnasArray, isMutant)
			if (isMutant) {
				response.status(HttpURLConnection.HTTP_OK)
			} else {
				response.status(HttpURLConnection.HTTP_FORBIDDEN)
			}
			apiService.saveStat(isMutant)
		} catch (e: RestMutantValidationException) {
			if (logger.isInfoEnabled) {
				logger.info("Se detectó un error de validación sobre los adn obtenidos." + e.message)
			}
			// Error de entidad no procesable, en este caso por fallo de validación (no existe en HttpURLConnection).
			response.status(422)
		} catch (e: JsonSyntaxException) {
			if (logger.isInfoEnabled) {
				logger.info("Se detectó un error de sintaxis en el mensaje obtenido. " + e.message)
			}
			response.status(HttpURLConnection.HTTP_BAD_REQUEST)
		} catch (e: Exception) {
			logger.error("Ocurrió un error al procesar el ingreso de adn via rest.", e)
			response.status(HttpURLConnection.HTTP_INTERNAL_ERROR)
		}
		return ""
	}

	/**
	 * Otorga las estadísticas de conteo de mutantes y humanos, junto con su relación.
	 * @param request El pedido del cliente.
	 * @param response La respuesta.
	 * @return Un json con las estadísticas solicitadas.
	 */
	private fun processStats(request: Request, response: Response): String {
		var result: String
		try {
			result = statsService.getJsonStats()
			response.status(HttpURLConnection.HTTP_OK)
		} catch (e: Exception) {
			logger.error("Ocurrió un error al generar las estadísticas a presentar.", e)
			response.status(HttpURLConnection.HTTP_INTERNAL_ERROR)
			result = "Ocurrió un error. Por favor, verifique el log."
		}
		return result
	}

	companion object {
		private val logger = LoggerFactory.getLogger(ApiRestController::class.java)
		private const val serverPort = 5000 // fixme: En variable de entorno
	}
}