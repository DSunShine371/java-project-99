plugins {
    application
    java
    checkstyle
    jacoco
    id("java")
    id("org.springframework.boot") version "3.5.5"
    id("io.spring.dependency-management") version "1.1.7"
    id("io.freefair.lombok") version "8.13"
    id("org.sonarqube") version "6.2.0.5505"
}

group = "hexlet.code"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

application {
    mainClass.set("hexlet.code.AppApplication")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-devtools")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.openapitools:jackson-databind-nullable:0.2.7")
    implementation("org.mapstruct:mapstruct:1.6.3")
    implementation("org.postgresql:postgresql:42.7.7")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.20.0")

    runtimeOnly("com.h2database:h2")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks {
    test {
        useJUnitPlatform()
        finalizedBy(jacocoTestReport)
    }

    jacocoTestReport {
        dependsOn(test)
        reports { xml.required.set(true) }
    }
}

tasks.test {
    useJUnitPlatform()
}

sonar {
    properties {
        property("sonar.projectKey", "DSunShine371_java-project-99")
        property("sonar.organization", "dsunshine371pis")
        property("sonar.sources", "src/main/java")
        property("sonar.tests", "src/test/java")
    }
}