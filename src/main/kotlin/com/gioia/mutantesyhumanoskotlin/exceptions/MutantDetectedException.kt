package com.gioia.mutantesyhumanoskotlin.exceptions

/**
 * Excepción que se libera ante la detección de un mutante.
 */
class MutantDetectedException(dna: String?) : Exception(dna)