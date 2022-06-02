import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    //id("org.springframework.boot") version "2.7.0"
    //id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version "1.6.21"
    kotlin("plugin.spring") version "1.6.21"
}

group = "com.gioia"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11
var kodein = "7.11.0"
var log = "2.0.0-alpha7"
//var spark = "1.0.0-alpha"
var gson = "2.9.0"
var mongo = "3.12.11"
var http4k = "4.25.16.2"

repositories {
    mavenCentral()
}

dependencies {
    //implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    //testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo")
    testImplementation(kotlin("test"))
    implementation("org.kodein.di:kodein-di-framework-compose:$kodein")
    implementation("org.slf4j:slf4j-log4j12:$log")
    //implementation("com.sparkjava:spark-kotlin:$spark")
    implementation("com.google.code.gson:gson:$gson")
    implementation("org.mongodb:mongo-java-driver:$mongo")
    implementation("org.http4k:http4k-core:$http4k")
    implementation("org.http4k:http4k-client-apache:$http4k")
    implementation("org.http4k:http4k-server-jetty:$http4k")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.test {
    useJUnitPlatform()
}
