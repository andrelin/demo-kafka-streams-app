package no.lindjo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class SampleStreamApplication

fun main(args: Array<String>) {
    runApplication<SampleStreamApplication>(*args)
}