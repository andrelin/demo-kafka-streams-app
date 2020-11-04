package no.lindjo.config

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.errors.LogAndContinueExceptionHandler
import java.util.*

open class KafkaConfiguration {
    var configuration: HashMap<String, Any>? = null
    private var properties: Properties? = null

    fun getProperties(): Properties {
        if (properties == null) {
            properties = Properties()
            for ((key, value) in configuration!!) {
                properties!![key] = value
            }
            setDefaults()
        }
        return properties as Properties
    }

    private fun setDefaults() {
        properties!!.putIfAbsent(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
        properties!!.putIfAbsent("schema.registry.url", "http://localhost:8081")
        properties!!.putIfAbsent(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        properties!!.putIfAbsent(StreamsConfig.REPLICATION_FACTOR_CONFIG, "3")
        properties!!.putIfAbsent(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, "0")

        //Do not fail on poison pill input
        properties!!.putIfAbsent(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, LogAndContinueExceptionHandler::class.java)
    }
}
