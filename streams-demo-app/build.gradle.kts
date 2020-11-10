import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.3.5.RELEASE"
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
    kotlin("jvm") version "1.3.72"
    kotlin("plugin.spring") version "1.3.72"
}

group = "no.lindjo"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

configurations {
    all {
        exclude(group = "junit", module = "junit")
        exclude(group = "log4j", module = "log4j")
    }
}

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        url = uri("https://packages.confluent.io/maven/")
    }
    jcenter()
}

object Versions {
    const val confluent = "5.5.1"
    const val kafka = "2.6.0"
}

dependencies {
    // External Application Dependencies
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-logging")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.kafka:spring-kafka")

    implementation("org.apache.kafka:kafka-streams:${Versions.kafka}")
    implementation("org.apache.kafka:kafka-clients:${Versions.kafka}")
//    implementation("io.confluent:kafka-schema-registry-client:${Versions.confluent}")
//    implementation("io.confluent:kafka-streams-avro-serde:${Versions.confluent}")
    implementation("io.confluent:kafka-schema-registry-client:${Versions.confluent}")
    implementation("io.confluent:kafka-streams-avro-serde:${Versions.confluent}")

    implementation("io.micrometer:micrometer-core")
    implementation("io.micrometer:micrometer-registry-prometheus")

    compileOnly("org.springframework.boot:spring-boot-configuration-processor")
    compileOnly("org.projectlombok:lombok")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.projectlombok:lombok")


    // External Test Dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }

    testAnnotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.apache.kafka:kafka-streams-test-utils:${Versions.kafka}")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}
