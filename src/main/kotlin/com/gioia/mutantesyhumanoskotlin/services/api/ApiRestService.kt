package com.gioia.mutantesyhumanoskotlin.services.api

import com.gioia.mutantesyhumanoskotlin.domain.DnaReceived
import com.gioia.mutantesyhumanoskotlin.domain.Stat
import com.gioia.mutantesyhumanoskotlin.exceptions.RestMutantValidationException
import com.google.gson.Gson
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.Updates
import org.slf4j.LoggerFactory
import java.util.regex.Pattern

class ApiRestService(
	private val mongoDatabase: MongoDatabase
): ApiService{
	private var logger = LoggerFactory.getLogger(ApiRestService::class.java)

	override fun validateDnaReceived(dna: Array<String>?){
		if(dna.isNullOrEmpty()){
			throw RestMutantValidationException("El listado de adn a verificar se encuentra vacío.")
		}

		//Se verifica por cada fila si corresponde con las letras indicadas.
		dna.forEach{
			if (!Pattern.compile(validInputRegex()).matcher(it).matches()) {
				throw RestMutantValidationException("El siguiente adn es inválido: $it")
			}
		}
	}

	override fun validInputRegex(): String{
		// De principio a fin, sólo se permite A, o T, o C, o G; una o más veces.
		return "^([ATCG]{1,})$"
	}

	override fun saveDnaReceived(dnaObject: Array<String>, isMutant: Boolean){
		try {
			mongoDatabase
				.getCollection(DnaReceived.collectionName)
				.insertOne(
						DnaReceived(dnaObject, isMutant).toDocument()
				)
		}
		catch (e: Exception){
			logger.error("Ocurrió un error al almacenar un registro para el adn recibido.", e)
			throw e
		}
	}

	override fun parseReceivedDna(body: String): Array<String>{
		try {
			return (Gson()
				.fromJson(body, Map::class.java)["dna"] as List<String>)
				.toTypedArray()
		}
		catch (e: Exception){
			logger.error("Ocurrió un error al interpretar el mensaje recibido.", e)
			throw e
		}
	}

	override fun saveStat(isMutant: Boolean){
		try {
			mongoDatabase
				.getCollection(Stat.collectionName)
				.updateOne(
					Filters.eq(Stat._id, Stat.id),
					Updates.inc((if(isMutant) Stat._mutantsQuantity else Stat._humansQuantity), 1),
					UpdateOptions().upsert(true)
				)
		}
		catch (e: Exception){
			logger.error("Ocurrió un error al incrementar la estadística.", e)
			throw e
		}
	}
}
