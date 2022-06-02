package com.gioia.mutantesyhumanoskotlin.config

import com.gioia.mutantesyhumanoskotlin.api.ApiRestController
import com.gioia.mutantesyhumanoskotlin.services.api.ApiRestService
import com.gioia.mutantesyhumanoskotlin.services.api.ApiService
import com.gioia.mutantesyhumanoskotlin.services.mutant.MutantService
import com.gioia.mutantesyhumanoskotlin.services.mutant.MutantVerificationService
import com.gioia.mutantesyhumanoskotlin.services.stats.MutantAndHumanStatsService
import com.gioia.mutantesyhumanoskotlin.services.stats.StatsService
import com.mongodb.MongoClient
import com.mongodb.MongoClientOptions
import com.mongodb.MongoClientURI
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance

val di = DI {
    bindSingleton<StatsService> {MutantAndHumanStatsService(instance())}
    bindSingleton<MutantService> {MutantVerificationService()}
    bindSingleton<ApiService> { ApiRestService(instance()) }
    bindSingleton {ApiRestController(instance(), instance(), instance())}
    bindSingleton {
        val mongoUser: String? = PropertiesReader.getProperty(Property.MONGO_USER.value)
        val mongoPassword: String? = PropertiesReader.getProperty(Property.MONGO_PASSWORD.value)
        val mongoUri = StringBuffer("mongodb://")

        if(listOf(mongoUser, mongoPassword).all{ it != null}) {
            mongoUri.append("$mongoUser:$mongoPassword@")
        }

        mongoUri.append(PropertiesReader.getProperty(Property.MONGO_HOST.value) ?: "localhost")
        .append("/")
        .append(PropertiesReader.getProperty(Property.MONGO_DATABASE_NAME.value)?: "mutantApp")
        //.append("?retryWrites=true&w=majority")

        val clientOptions = MongoClientOptions.Builder()
        .minConnectionsPerHost(PropertiesReader.getProperty(Property.MONGO_MIN_CONNECTIONS.value)?.toInt() ?: 10)
        .connectionsPerHost(PropertiesReader.getProperty(Property.MONGO_MAX_CONNECTIONS.value)?.toInt() ?: 2990)
        .maxConnectionIdleTime(PropertiesReader.getProperty(Property.MONGO_TIMEOUT.value)?.toInt() ?: 30000)

        MongoClient(MongoClientURI(mongoUri.toString(), clientOptions))
            .getDatabase(PropertiesReader.getProperty(Property.MONGO_DATABASE_NAME.value) ?: "mutantApp")
    }
}
