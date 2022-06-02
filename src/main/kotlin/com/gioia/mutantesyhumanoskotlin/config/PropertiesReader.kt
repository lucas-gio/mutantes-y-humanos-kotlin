package com.gioia.mutantesyhumanoskotlin.config

import java.util.*

object PropertiesReader {
    private val properties = Properties()
    private const val CONFIG = "application.properties"

    init {
        val file = this::class.java.classLoader.getResourceAsStream(CONFIG)
        properties.load(file)
    }

    fun getProperty(key: String): String? = properties.getProperty(key)
}