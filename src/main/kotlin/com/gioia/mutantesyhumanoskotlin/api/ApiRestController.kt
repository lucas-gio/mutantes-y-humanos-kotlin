package com.gioia.mutantesyhumanoskotlin.api

import com.gioia.mutantesyhumanoskotlin.exceptions.RestMutantValidationException
import com.gioia.mutantesyhumanoskotlin.services.api.ApiService
import com.gioia.mutantesyhumanoskotlin.services.mutant.MutantService
import com.gioia.mutantesyhumanoskotlin.services.stats.StatsService
import com.google.gson.JsonSyntaxException
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.FORBIDDEN
import org.http4k.core.Status.Companion.INTERNAL_SERVER_ERROR
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.UNPROCESSABLE_ENTITY
import org.slf4j.LoggerFactory

/**
 * Controlador dedicado a los ingresos al sistema por api rest.
 */
class ApiRestController(
	private val apiService: ApiService,
	private val mutantService: MutantService,
	private val statsService: StatsService
) {
	/**
	 * Procesa el ingreso de adn para verificar si es un humano o mutante.
	 * En caso de ser mutante o humano almacena un incremento para visualizarlo
	 * posteriormente como estadística.
	 * @param request El pedido del cliente.
	 * @return Un cuerpo vacío y un estado http acorde al resultado obtenido.
	 */
	fun processMutantPost(request: Request): Response {
		var response: Response
		try {
			val dnasArray = apiService.parseReceivedDna(request.body.toString())
			apiService.validateDnaReceived(dnasArray)

			val isMutant = mutantService.isMutant(dnasArray)
			apiService.saveDnaReceived(dnasArray, isMutant)
			response = Response(if (isMutant) OK else FORBIDDEN)
			apiService.saveStat(isMutant)
		}
		catch (e: RestMutantValidationException) {
			logger.atInfo().log("Se detectó un error de validación sobre los adn obtenidos.$e.message")
			// Error de entidad no procesable, en este caso por fallo de validación (no existe en HttpURLConnection).
			response = Response(UNPROCESSABLE_ENTITY)
		}
		catch (e: JsonSyntaxException) {
			logger.atInfo().log("Se detectó un error de sintaxis en el mensaje obtenido. $e.message")
			response = Response(BAD_REQUEST)
		}
		catch (e: Exception) {
			logger.error("Ocurrió un error al procesar el ingreso de adn via rest.", e)
			response = Response(INTERNAL_SERVER_ERROR)
		}

		return response
	}

	/**
	 * Otorga las estadísticas de conteo de mutantes y humanos, junto con su relación.
	 * @return Un json con las estadísticas solicitadas.
	 */
	fun processStats(): Response {
		val response = try {
			Response(OK).body(
				statsService.getJsonStats()
			)
		}
		catch (e: Exception) {
			logger.error("Ocurrió un error al generar las estadísticas a presentar.", e)
			Response(INTERNAL_SERVER_ERROR).body("Ocurrió un error. Por favor, verifique el log.")
		}

		return response
	}

	companion object {
		private val logger = LoggerFactory.getLogger(ApiRestController::class.java)
	}
}