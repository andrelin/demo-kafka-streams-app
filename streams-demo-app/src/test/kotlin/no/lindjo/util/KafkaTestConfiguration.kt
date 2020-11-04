package no.lindjo.util

import no.lindjo.config.KafkaConfiguration
import org.apache.kafka.streams.StreamsConfig


abstract class KafkaTestConfiguration : KafkaConfiguration() {
    init {
        val configuration = HashMap<String, Any>()
        configuration[StreamsConfig.APPLICATION_ID_CONFIG] = ""
        configuration["schema.registry.url"] = "bogus"
        configuration[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = "bogus"
        this.configuration = configuration
    }
}
