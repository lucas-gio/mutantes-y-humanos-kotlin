package com.gioia.mutantesyhumanoskotlin.domain

import com.google.gson.Gson
import java.math.BigDecimal

/**
 * Clase que representa una estadística de cantidad de humanos, mutantes, y su relación.
 */
class MutantAndHumanStat(countMutantDna: Int, countHumanDna: Int, ratio: BigDecimal?) {
	private var countMutantDna: Int = 0
	private var countHumanDna: Int = 0
	private var ratio: BigDecimal? = null

	init {
		this.countMutantDna = countMutantDna
		this.countHumanDna = countHumanDna
		this.ratio = ratio
	}

	fun toJson(): String {
		val result = mapOf(
			"count_mutant_dna" to countMutantDna,
			"count_human_dna" to countHumanDna,
			"ratio" to ratio
		)

		return Gson().toJson(result)
	}
}