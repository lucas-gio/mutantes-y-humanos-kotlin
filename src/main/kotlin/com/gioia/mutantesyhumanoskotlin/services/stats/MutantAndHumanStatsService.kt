package com.gioia.mutantesyhumanoskotlin.services.stats

import com.gioia.mutantesyhumanoskotlin.domain.MutantAndHumanStat
import com.gioia.mutantesyhumanoskotlin.domain.Stat
import com.gioia.mutantesyhumanoskotlin.services.mongo.AppMongoClient
import com.mongodb.client.model.Filters.eq
import org.bson.Document
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.math.RoundingMode

class MutantAndHumanStatsService : StatsService{
	private var logger = LoggerFactory.getLogger(MutantAndHumanStatsService::class.java)

	override fun getJsonStats(): String{
		try {
			val statCountersList = AppMongoClient
				.getDb()
				.getCollection(Stat.collectionName)
				.find(
					eq(Stat._id, Stat.id)
				)
				.first()

			val statCounters: Document
			var mutantCount = 0
			var humanCount = 0

			if(statCountersList.iterator().hasNext()){
				statCounters = statCountersList.iterator().next()
				mutantCount = statCounters.getInteger(Stat._mutantsQuantity, 0)
				humanCount = statCounters.getInteger(Stat._humansQuantity, 0)
			}

			var ratio = BigDecimal(mutantCount)

			if (humanCount > 0) {
				ratio = ratio.divide(BigDecimal(humanCount), 2, RoundingMode.HALF_EVEN)
			}

			return MutantAndHumanStat(mutantCount, humanCount, ratio).toJson()
		}
		catch (e: Exception){
			logger.error("Ocurrió un error al procesar las estadísticas de mutantes y humanos.", e)
			throw e
		}
	}
}