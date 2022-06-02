package com.gioia.mutantesyhumanoskotlin.config

enum class Property (
    val value: String
){
    MONGO_HOST("mongoHost"),
    MONGO_DATABASE_NAME("mongoDatabaseName"),
    MONGO_USER("mongoUser"),
    MONGO_PASSWORD("mongoPassword"),
    MONGO_TIMEOUT("mongoTimeout"),
    MONGO_MIN_CONNECTIONS("mongoMinConnectionPerHost"),
    MONGO_MAX_CONNECTIONS("mongoMaxConnectionPerHost"),
    SERVER_PORT("serverPort")
}