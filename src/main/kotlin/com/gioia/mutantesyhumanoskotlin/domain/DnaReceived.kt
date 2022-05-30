package com.gioia.mutantesyhumanoskotlin.domain

import org.bson.Document
import org.bson.types.ObjectId

/**
 * Clase que representa un registro a almacenar de adn recibido.
 */
class DnaReceived(content: Array<String>, isMutant: Boolean) {
	private var id: String? = null

	// El array de adns recibidos.
	private var content: Array<String>
	private var isMutant = false

	init {
		this.isMutant = isMutant
		this.content = content
		id = ObjectId().toString()
	}

	fun toDocument(): Document {
		val document = Document("_id", id)
		document["dna"] = listOf(*content)
		document["isMutant"] = isMutant
		return document
	}

	companion object {
		var collectionName = "dna"
	}
}