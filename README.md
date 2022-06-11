![](cover2.jpg) 

[![build](https://github.com/lucas-gio/mutantes-y-humanos-kotlin/actions/workflows/build.yml/badge.svg)](https://github.com/lucas-gio/mutantes-y-humanos-kotlin/actions/workflows/build.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=lucas-gio_mutantes-y-humanos-kotlin&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=lucas-gio_mutantes-y-humanos-kotlin)
[![Discussion](https://img.shields.io/badge/chat-Discussion-blueviolet)](https://github.com/lucas-gio/mutantes-y-humanos-kotlin/discussions)

# Mutantes y Humanos Kotlin - Lucas-Gioia
Sistema de procesamiento de adn de mutantes y humanos, reescrita en kotlin [de la original realizada como examen de ingreso para mercadolibre en java](https://github.com/lucas-gio/mutantes-y-humanos).

# Requerimiento:
[Requerimiento para realizar el sistema](https://github.com/lucas-gio/mutantes-y-humanos-kotlin/blob/main/documentation/Examen%20Mercadolibre%202020%20-%20Mutantes.pdf)

# EndPoints rest: 
| Método | Endpoint | Detalle |
| ------ | ------ | ------ |
| POST | /mutant |  Procesa el adn enviado en el cuerpo del mensaje |
| GET | /stats |  Obtiene las estadísticas de los adn enviados |

## POST /mutant: Envío de adn

Para realizar el envío de adn debe generarse una petición POST enviando en el cuerpo del mensaje un valor como el especificado debajo.
```sh
{"dna":["ATGCGA","CAGTGC","TTATGT","AGAAGG","CCCCTA","TCACTG"]}
```
  
Una vez finalizado el procesamiento, el sistema responderá con uno de los siguientes estados http: 
| Estado | Motivo |
| ------ | ------ |
| 200 | Fué detectado un mutante|
| 400 | Fué detectado un error en el formato json|
| 403 | Fué detectado un humano|
| 422 | Fué detectado un ingreso inválido|
| 500 | Fué detectado un error genérico|

Se considera inválido un ingreso con dna null, sin elementos, o compuesto con dígitos que no son los especificados en el requerimiento (A,T,C,G).

Ante cada ingreso válido, previo al momento de generar la respuesta, se almacena un registro en una base de datos mongodb, con los datos obtenidos. El siguiente es un ejemplo de un registro almacenado:

```sh
{"_id":"5efbe231e22baa065f61806b","dna":["ATGCGA","CAGTGC","TTATTT","AGACGG","GCGTCA","TCACTG"],"isMutant":false}
  ```
  
## GET /stats: Estadísticas

Para obtener las estadísticas de humanos y mutantes debe realizarse una petición GET.
Se obtiene como resultado un json que sigue el formato propuesto:
  ```sh
  {"count_mutant_dna":4,"count_human_dna":10,"ratio":0.40}
  
   ```  
   
## Ejecución y prerequisitos
Para ejecutar el programa se debe lanzar el main desde la clase com.application.Application.
El archivo application.properties mantiene las configuraciones relacionadas a la base de datos. El puerto por defecto es el 5000.
Para compilar el sistema, se debe situarse en la carpeta del proyecto, y luego ejecutar mvn clean, y luego mvn install. El jar resultante, que se encuentra en /target/MercadoLibreLucasGioia-1.0-SNAPSHOT-shaded.jar contiene tanto la aplicación, como los archivos de recurso, y las dependencias.

## Configuraciones disponibles

| Nombre | Descripción | Valor por defecto |
| ------ | ------ | ------ |
| mongoHost | El host donde reside mongodb | localhost |
| mongoDatabaseName | El nombre de la base de datos | mutantApp |
| mongoUser | El usuario de la base de datos | (por defecto no utiliza) |
| mongoPassword | La contraseña de la base de datos | (por defecto no utiliza) |
| mongoTimeout | Tiempo de espera para conectar a mongodb | 30000 ms |
| mongoMinConnectionPerHost | Cantidad de conexiones mínimas que tendrá el pool de mongodb | 10 |
| mongoMaxConnectionPerHost | Cantidad de conexiones máximo que tendrá el pool de mongodb | 2990 |
| serverPort | Puerto en que estará disponible la aplicación | 5000 |

## Tecnologías aplicadas
- [Kodein DI](https://kodein.org/di/) - Inyección de dependencias
- [Slf4j - log4j12](https://www.slf4j.org/) - Api de logging con log4j como implementación
- [Google Gson](https://github.com/google/gson) - Intérprete json
- [Mongodb](https://www.mongodb.com/) - Base de datos
- [Http4k](https://www.http4k.org/) - Servicios rest y sus tests
- [Jetty](https://www.eclipse.org/jetty/) - Servidor de la aplicación, provisto por htt4k
- [Kotlin](https://kotlinlang.org/) - Lenguaje de programación
