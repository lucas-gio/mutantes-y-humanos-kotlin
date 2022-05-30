package com.gioia.mutantesyhumanoskotlin.services.mutant

/**
 * Interfaz destinada a mutantes.
 */
interface MutantService {
    /**
     * Determina si el adn tomado por parámetro corresponde a un mutante o no.
     * @param dna El adn correspondiente al ser a evaluar.
     * @return true si es mutante, false en caso contrario.
     */
    fun isMutant(dna: Array<String>): Boolean
}
