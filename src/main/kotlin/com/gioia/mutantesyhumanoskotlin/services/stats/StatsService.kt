package com.gioia.mutantesyhumanoskotlin.services.stats;

/**
 * Interfaz destinada a estadísticas.
 */
interface StatsService {

	/**
	 * Permite obtener las estadísticas.
	 * @return Un json con las estadísticas.
	 */
	fun getJsonStats(): String
}
