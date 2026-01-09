import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    kotlin("jvm") version "2.2.21"
    id("org.openapi.generator") version "7.18.0"
    id("org.springframework.boot") version "4.0.1"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("plugin.spring") version "2.2.21"
    `maven-publish`
}

group = "clockify2jira"
version = "1.0.0"

kotlin.sourceSets["main"].kotlin.srcDir(layout.buildDirectory.dir("generated/openapi/src/main/kotlin"))

val coroutinesVersion = "1.6.4"
val fasterxmlJacksonVersion = "2.20.1"

repositories {
    mavenCentral()
    maven("https://packages.atlassian.com/repository/public")
}

dependencies {
    implementation( "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation( "org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$coroutinesVersion")
    implementation( "org.jetbrains.kotlinx:kotlinx-coroutines-debug:$coroutinesVersion")
    implementation("com.fasterxml.jackson.core:jackson-core:$fasterxmlJacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$fasterxmlJacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$fasterxmlJacksonVersion")
    implementation("com.squareup.okhttp3:okhttp:5.3.2")
    testImplementation(kotlin("test"))
    implementation("io.github.microutils:kotlin-logging:3.0.5")

    // Spring Boot dependencies
    implementation("org.springframework.boot:spring-boot-starter")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    implementation("com.atlassian.jira:jira-api:10.6.1")
    implementation("commons-httpclient:commons-httpclient:3.1")
    implementation("com.atlassian.jira:jira-rest-java-client-api:7.0.0-BBSDEV-33699-dev1")
    implementation("com.atlassian.jira:jira-rest-java-client-core:7.0.0-BBSDEV-33699-dev1")
    implementation("jakarta.ws.rs:jakarta.ws.rs-api:4.0.0")
}



tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
        jvmTarget.set(JvmTarget.JVM_21)
    }
    dependsOn("openApiGenerateAll")
}

kotlin {
    jvmToolchain(21)
}

tasks.register("openApiGenerateAll") {
    group = "generation"
    description = "Generates stubs and models from all supported OpenAPI specs"
    dependsOn("openApiGenerateClockify")
}

tasks.register("openApiGenerateClockify", GenerateTask::class) {
    group = "generation"
    generatorName = "kotlin"
    inputSpec = File("${projectDir}/api/clockify/clockify-openapi.yaml").toURI().toString()
    outputDir = "${buildDir}/generated/openapi"
    apiPackage = "clockify2jira.clockify.api"
    modelPackage = "clockify2jira.clockify.model"
    configOptions = mapOf(
        "exceptionHandler" to "false",
        "serializationLibrary" to "jackson",
        "dateLibrary" to "java8",
        "disallowAdditionalPropertiesIfNotPresent" to "false",
        "hideGenerationTimestamp" to "true",
        "enumPropertyNaming" to "UPPERCASE",
        "interfaceOnly" to "true",
        "useTags" to "true",
        "documentationProvider" to "none",
        "useSpringBoot3" to "true"
    )
}

tasks.jar {
    enabled = false
}

tasks.bootJar {
    archiveFileName.set("clockify2jira-$version.jar")
}
