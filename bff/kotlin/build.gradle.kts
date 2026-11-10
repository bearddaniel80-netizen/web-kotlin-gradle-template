
plugins {
    java
    id("org.springframework.boot") version "3.5.7"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.spring") version "2.2.21"
}

group = "com.example"
version = "1.0.0"
description = "Spring Boot Web Application"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Web (REST APIs and embedded Tomcat)
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Spring Boot Actuator (health checks and monitoring)
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Optional: Validation support
    // implementation("org.springframework.boot:spring-boot-starter-validation")

    // Development tools
    // developmentOnly("org.springframework.boot:spring-boot-devtools")

    // Kotlin support
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // Testing
    // testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}

/*
tasks.named<Test>("test") {
    useJUnitPlatform()
}
*/
