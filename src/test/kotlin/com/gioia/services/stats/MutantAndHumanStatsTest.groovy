package com.gioia.services.stats

import com.application.domain.Stat
import com.application.services.mongo.AppMongoClient
import com.mongodb.BasicDBObject
import org.bson.Document
import spock.lang.Specification

/**
 * Pruebas relacionadas al servicio de estadísticas de humanos y mutantes.
 */
class MutantAndHumanStatsTest extends Specification {
	MutantAndHumanStatsService mutantAndHumanStatsService = new MutantAndHumanStatsService()

	def "Se prueba que se obtengan valores válidos al pedir estadísticas llamando a getJsonStats() "(){
		given: "Una colección de estadísticas vacía"
		cleanAllStatCollection()

		when: "No hay registros preexistentes y se consulta la estadística"
		String stat = mutantAndHumanStatsService.getJsonStats()

		then: "Se obtiene el resultado esperado"
		stat == '{"count_mutant_dna":0,"count_human_dna":0,"ratio":0}'

		when: "Hay 1 humano y no hay mutantes y se consulta la estadística"
		Document document = new Document()
		document.put(Stat._humansQuantity,1)
		document.put(Stat._mutantsQuantity,0)
		document.put(Stat._id, Stat.id)

		AppMongoClient.getDb().getCollection(Stat.collectionName).insertOne(document)

		stat = mutantAndHumanStatsService.getJsonStats()

		then: "Se obtiene el resultado esperado"
		stat == '{"count_mutant_dna":0,"count_human_dna":1,"ratio":0.00}'

		when: "Hay 1 mutante y no hay humanos, y se consulta la estadística"
		cleanAllStatCollection()

		document = new Document()
		document.put(Stat._humansQuantity,0)
		document.put(Stat._mutantsQuantity,1)
		document.put(Stat._id, Stat.id)

		AppMongoClient.getDb().getCollection(Stat.collectionName).insertOne(document)

		stat = mutantAndHumanStatsService.getJsonStats()

		then: "Se obtiene el resultado esperado"
		stat == '{"count_mutant_dna":1,"count_human_dna":0,"ratio":1}'

		when: "Hay 4 mutantes y 10 humanos, y se consulta la estadística"
		cleanAllStatCollection()

		document = new Document()
		document.put(Stat._humansQuantity,10)
		document.put(Stat._mutantsQuantity,4)
		document.put(Stat._id, Stat.id)

		AppMongoClient.getDb().getCollection(Stat.collectionName).insertOne(document)

		stat = mutantAndHumanStatsService.getJsonStats()

		then: "Se obtiene el resultado esperado"
		stat == '{"count_mutant_dna":4,"count_human_dna":10,"ratio":0.40}'

		cleanup: "Se eliminan los datos generados"
		cleanAllStatCollection()
	}

	/**
	 * Borra toda la colección dna received
	 */
	private void cleanAllStatCollection(){
		AppMongoClient.getDb().getCollection(Stat.collectionName).deleteMany(new BasicDBObject())
	}

}
