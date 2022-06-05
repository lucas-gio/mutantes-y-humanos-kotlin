package com.gioia.mutantesyhumanoskotlin.services.api

/**
 * Interfaz destinada a servicios de api.
 */
interface ApiService {

	/**
	 * Realiza la validación del ingreso de adn. Libera RestMutantValidationException en caso de
	 * no ser válido el ingreso.
	 * @param dna El array de adn a verificar.
	 */
	fun validateDnaReceived(dna: Array<String>?)

	/**
	 * Almacena el adn recibido en la base de datos.
	 * @param dnaObject El array de adn a almacenar.
	 * @param isMutant true si es mutante.
	 */
	fun saveDnaReceived(dnaObject: Array<String>, isMutant: Boolean)

	/**
	 * Toma el cuerpo del mensaje recibido por rest, y lo procesa, retornando el array de adn.
	 * @param body El cuerpo del mensaje recibido.
	 * @return El array de adn.
	 */
	fun parseReceivedDna(body: String): Array<String>

	/**
	 * Almacena un incremento en la tabla de estadísticas de humanos y mutantes.
	 * @param isMutant
	 */
	fun saveStat(isMutant: Boolean)
}
