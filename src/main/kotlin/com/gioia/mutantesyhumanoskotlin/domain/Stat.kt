package com.gioia.mutantesyhumanoskotlin.domain

/**
 * Clase que representa un contador de humanos y mutantes en la bd.
 */
object Stat {
    var collectionName = "stat"

    // Sólo habrá un documento.
    var id = "1"
    var fieldId = "_id"
    var fieldMutantsQuantity = "mutantsQuantity"
    var fieldHumansQuantity = "humansQuantity"
}