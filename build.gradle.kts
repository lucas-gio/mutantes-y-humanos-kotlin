import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.7.0"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version "1.6.21"
    kotlin("plugin.spring") version "1.6.21"
}

group = "com.gioia"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11
var nitrite = "3.4.4"
var kodein = "7.11.0"
var log = "2.0.0-alpha7"
var spark = "1.0.0-alpha"
var gson = "2.9.0"
var mongo = "4.6.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo")
    implementation("org.dizitart:nitrite:$nitrite")
    implementation("org.kodein.di:kodein-di-framework-compose:$kodein")
    implementation("org.slf4j:slf4j-log4j12:$log")
    implementation("com.sparkjava:spark-kotlin:$spark")
    implementation("com.google.code.gson:gson:$gson")
    implementation("org.mongodb:mongodb-driver-legacy:$mongo")
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
