package com.gioia.mutantesyhumanoskotlin.services.stats

import com.gioia.mutantesyhumanoskotlin.config.di
import com.gioia.mutantesyhumanoskotlin.domain.Stat
import com.mongodb.BasicDBObject
import com.mongodb.client.MongoDatabase
import org.bson.Document
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.kodein.di.instance

/**
 * Pruebas relacionadas al servicio de estadísticas de humanos y mutantes.
 */
class MutantAndHumanStatsTest{
	private val statsService: StatsService by di.instance()
	private val mongoDatabase: MongoDatabase by di.instance()
	@Test
	fun `Se prueba que se obtengan valores válidos al pedir estadísticas llamando a getJsonStats()`(){
		//given: "Una colección de estadísticas vacía"
		cleanAllStatCollection()

		//when: "No hay registros preexistentes y se consulta la estadística"
		//then: "Se obtiene el resultado esperado"
		Assertions.assertEquals(statsService.getJsonStats() , """{"count_mutant_dna":0,"count_human_dna":0,"ratio":0}""")

		//when: "Hay 1 humano y no hay mutantes y se consulta la estadística"
		mongoDatabase.getCollection(Stat.collectionName).insertOne(
			Document(mapOf<String, Any>(
				Stat.fieldHumansQuantity to 1,
				Stat.fieldMutantsQuantity to 0,
				Stat.fieldId to Stat.id
			))
		)

		//then: "Se obtiene el resultado esperado"
		Assertions.assertEquals(statsService.getJsonStats() , """{"count_mutant_dna":0,"count_human_dna":1,"ratio":0.00}""")

		//when: "Hay 1 mutante y no hay humanos, y se consulta la estadística"
		cleanAllStatCollection()

		mongoDatabase.getCollection(Stat.collectionName).insertOne(
			Document(mapOf<String, Any>(
				Stat.fieldHumansQuantity to 0,
				Stat.fieldMutantsQuantity to 1,
				Stat.fieldId to Stat.id
			))
		)

		//then: "Se obtiene el resultado esperado"
		Assertions.assertEquals(statsService.getJsonStats(),  """{"count_mutant_dna":1,"count_human_dna":0,"ratio":1}""" )

		//when: "Hay 4 mutantes y 10 humanos, y se consulta la estadística"
		cleanAllStatCollection()

		mongoDatabase.getCollection(Stat.collectionName).insertOne(
			Document(mapOf<String, Any>(
				Stat.fieldHumansQuantity to 10,
				Stat.fieldMutantsQuantity to 4,
				Stat.fieldId to Stat.id
			))
		)

		//then: "Se obtiene el resultado esperado"
		Assertions.assertEquals(statsService.getJsonStats(), """{"count_mutant_dna":4,"count_human_dna":10,"ratio":0.40}""")

		//cleanup: "Se eliminan los datos generados"
		cleanAllStatCollection()
	}

	/**
	 * Borra toda la colección dna received
	 */
	private fun cleanAllStatCollection(){
		mongoDatabase
			.getCollection(Stat.collectionName)
			.deleteMany(BasicDBObject())
	}

}
