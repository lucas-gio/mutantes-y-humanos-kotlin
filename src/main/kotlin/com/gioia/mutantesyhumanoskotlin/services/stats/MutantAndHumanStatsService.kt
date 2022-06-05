package com.gioia.mutantesyhumanoskotlin.services.stats

import com.gioia.mutantesyhumanoskotlin.domain.MutantAndHumanStat
import com.gioia.mutantesyhumanoskotlin.domain.Stat
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters.eq
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.math.RoundingMode

class MutantAndHumanStatsService(
	private val mongoDatabase: MongoDatabase
) : StatsService{
	private var logger = LoggerFactory.getLogger(MutantAndHumanStatsService::class.java)

	override fun getJsonStats(): String{
		try {
			val statistics = mongoDatabase
				.getCollection(Stat.collectionName)
				.find(
					eq(Stat.fieldId, Stat.id)
				)
				.limit(1)
				.first()

			val mutantCount = (statistics?.get(Stat.fieldMutantsQuantity) ?: 0) as Int
			val humanCount = (statistics?.get(Stat.fieldHumansQuantity) ?: 0) as Int

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