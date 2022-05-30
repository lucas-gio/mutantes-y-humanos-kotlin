package com.gioia.mutantesyhumanoskotlin.services.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;

/**
 * Clase singleton para reutilizar el pool de conexiones de mongodb.
 */
public final class AppMongoClient {
	private static volatile MongoClient instance = null;
	private static volatile String databaseName = null;
	private static final Logger LOG = LoggerFactory.getLogger(AppMongoClient.class);
	private static final String CONFIG_PROPERTIES = "application.properties";
	private static final String MONGO_HOST = "mongoHost";
	private static final String MONGO_DATABASE_NAME = "mongoDatabaseName";
	private static final String MONGO_USER = "mongoUser";
	private static final String MONGO_PASSWORD = "mongoPassword";

	private AppMongoClient() {}

	/**
	 * Retorna el objeto mongoclient para utilizar con diversas bases de datos.
	 * @return La instancia MongoClient.
	 * @throws Exception Se libera ante algún error genérico.
	 */
	public static MongoClient getInstance() throws Exception{
		if (instance == null) {
			synchronized(MongoClient.class) {
				if (instance == null) {

					// Se crea mongoclient en base a las configuraciones tomadas del archivo properties.
					try (InputStream input = AppMongoClient.class.getClassLoader().getResourceAsStream(CONFIG_PROPERTIES)) {
						Properties prop = new Properties();
						prop.load(input);

						databaseName = prop.getProperty(MONGO_DATABASE_NAME);

						StringBuffer mongoUri = new StringBuffer("mongodb+srv://");
						mongoUri.append(prop.getProperty(MONGO_USER));
						mongoUri.append(":");
						mongoUri.append(prop.getProperty(MONGO_PASSWORD));
						mongoUri.append("@");
						mongoUri.append(prop.getProperty(MONGO_HOST));
						mongoUri.append("/");
						mongoUri.append(databaseName);
						mongoUri.append("?retryWrites=true&w=majority");

						if(LOG.isDebugEnabled()){ LOG.debug("Conectando a " + mongoUri.toString()); }

						MongoClientOptions.Builder clientOptions = new MongoClientOptions.Builder();
						// La cantidad de conexiones que tendrá el pool de mongodb.
						clientOptions.minConnectionsPerHost(10);
						clientOptions.connectionsPerHost(2990);
						// Tiempo de espera de 60 seg como máximo.
						clientOptions.maxConnectionIdleTime(60000);

						instance = new MongoClient(new MongoClientURI(mongoUri.toString(), clientOptions));
					}
					catch (Exception e){
						LOG.error("Ocurrió un error al crear la conexión a la base de datos.", e);
						throw e;
					}
				}
			}
		}
		return instance;
	}

	/**
	 * Selecciona la base de datos configurada y retorna su referencia.
	 * @return La referencia a utilizar con getCollection u otros.
	 * @throws Exception Se libera ante una excepción genérica.
	 */
	public static MongoDatabase getDb() throws Exception{
		try {
			return getInstance().getDatabase(databaseName);
		}
		catch (Exception e){
			LOG.error("Ocurrió un error al obtener la referencia a la base de datos.", e);
			throw e;
		}
	}
}