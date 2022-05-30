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
import com.mongodb.client.MongoDatabase
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance


val di = DI {
    bindSingleton<StatsService> {MutantAndHumanStatsService()}
    bindSingleton<MutantService> {MutantVerificationService()}
    bindSingleton<ApiService> { ApiRestService(instance()) }
    bindSingleton {ApiRestController(instance(), instance(), instance())}
    bindSingleton {
        val mongoUri = StringBuffer("mongodb+srv://")
        //mongoUri.append(prop.getProperty(AppMongoClient.MONGO_USER)) //fixme variables de entorno.
        mongoUri.append(":")
        //mongoUri.append(prop.getProperty(AppMongoClient.MONGO_PASSWORD)) //fixme variables de entorno.
        mongoUri.append("@")
        //mongoUri.append(prop.getProperty(AppMongoClient.MONGO_HOST)) //fixme variables de entorno.
        mongoUri.append("/")
        // mongoUri.append(AppMongoClient.databaseName) //fixme variables de entorno.
        mongoUri.append("?retryWrites=true&w=majority")

        val clientOptions = MongoClientOptions.Builder()
        // La cantidad de conexiones que tendrá el pool de mongodb.
        // La cantidad de conexiones que tendrá el pool de mongodb.
        clientOptions.minConnectionsPerHost(10)
        clientOptions.connectionsPerHost(2990)
        // Tiempo de espera de 60 seg como máximo.
        // Tiempo de espera de 60 seg como máximo.
        clientOptions.maxConnectionIdleTime(60000)
        MongoClient(MongoClientURI(mongoUri.toString(), clientOptions))
            .getDatabase("mongo") //fixme
    }
    /*bindSingleton<DatabaseGenerator> {DatabaseGeneratorImpl(instance(), instance())}
    bindSingleton<CountryRepository> {CountryRepositoryImpl(instance())}
    bindSingleton<ConfigurationRepository> {ConfigurationRepositoryImpl(instance())}
    bindSingleton<PlayerService> {PlayerServiceImpl(instance())}
    bindSingleton<MessageService> {MessageServiceImpl(instance())}
    //bindSingleton<StateKeeper> {()}
    bindSingleton {AudioPlayerComponent()}
    bindSingleton {StationsViewModel(instance(), instance(), instance())}

    //val bundle: ResourceBundle = ResourceBundle.getBundle("Messages")
    bindSingleton<Nitrite>{
        Nitrite
            .builder()
            .filePath(".${File.separator}file.db")
            .openOrCreate()
    }
    bindConstant(tag = "defaultHeight") {500}
    bindConstant(tag = "defaultWidth") {900}*/
}
