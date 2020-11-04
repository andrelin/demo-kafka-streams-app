package no.lindjo.config

import no.lindjo.BlogStreamBuilder
import org.slf4j.LoggerFactory
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.stereotype.Component

@Component
class StreamsHealthCheck(
        private val streamBuilder: BlogStreamBuilder
) : HealthIndicator {

    private val logger = LoggerFactory.getLogger(StreamsHealthCheck::class.java)

    override fun health(): Health {
        return when (streamBuilder.streams.state().isRunningOrRebalancing) {
            true -> {
                Health.up().withDetail("kafka-streams", streamBuilder.streams.state()).build()
            }
            false -> {
                val errorMsg = "Topology not running, restart Application"
                logger.error(errorMsg)
                Health.down().withDetail("kafka-streams", streamBuilder.streams.state())
                        .withDetail("info", errorMsg)
                        .build()
            }
        }
    }
}