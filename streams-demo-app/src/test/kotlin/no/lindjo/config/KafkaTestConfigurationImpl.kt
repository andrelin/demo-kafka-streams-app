package no.lindjo.config

import no.lindjo.util.KafkaTestConfiguration
import org.apache.kafka.common.serialization.Serdes

class KafkaTestConfigurationImpl : KafkaTestConfiguration() {
    init {
        val properties = getProperties()
        properties["default.key.serde"] = Serdes.StringSerde::class.java
        properties["default.value.serde"] = Serdes.StringSerde::class.java
    }
}