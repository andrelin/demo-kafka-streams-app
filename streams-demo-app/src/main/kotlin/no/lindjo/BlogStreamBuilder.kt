package no.lindjo

import no.lindjo.config.KafkaConfiguration
import no.lindjo.config.KafkaInputTopics
import no.lindjo.config.KafkaOutputTopics
import no.lindjo.util.TopicsAdmin
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.Topology
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.*
import javax.annotation.PostConstruct

@Component
class BlogStreamBuilder(
        private val kafkaInputTopics: KafkaInputTopics,
        private val kafkaOutputTopics: KafkaOutputTopics,
        private val kafkaConfiguration: KafkaConfiguration,
        private val topologyBuilderBuilder: BlogTopologyBuilder
) {
    private val logger = LoggerFactory.getLogger(BlogStreamBuilder::class.java)

    lateinit var streams: KafkaStreams
    lateinit var topicsAdmin: TopicsAdmin

    @PostConstruct
    fun runStream() {
        val applicationId = kafkaConfiguration.getProperties().getProperty(StreamsConfig.APPLICATION_ID_CONFIG)
        logger.info("{} has the following config {}", applicationId, kafkaConfiguration.getProperties())
        createOrModifyTopics()

        val topology: Topology = topologyBuilderBuilder.buildTopology()
        val streams = KafkaStreams(topology, kafkaConfiguration.getProperties())

        logger.info("{} has the following Topology: {} ", applicationId, topology.describe())
        streams.setUncaughtExceptionHandler { _, throwable: Throwable? -> logger.error("Error in stream: ", throwable) }
        Runtime.getRuntime().addShutdownHook(Thread { streams.close() })

        logger.info("Starting {}", applicationId)
        streams.start()
    }

    fun getTopicsToCreateOrModify(): List<TopicConfiguration> {
        return listOf(
                kafkaInputTopics.users,
                kafkaInputTopics.posts,
                kafkaInputTopics.subscriptions,
                kafkaOutputTopics.userInfo,
                kafkaOutputTopics.userSubscriptionCount,
                kafkaOutputTopics.eventMails
        )
    }

    private fun createOrModifyTopics() {
        val adminClient: AdminClient = AdminClient.create(kafkaConfiguration.getProperties())
        val createTopics: MutableList<TopicConfiguration> = ArrayList()
        val topicsAdmin = TopicsAdmin(adminClient)

        for (t in getTopicsToCreateOrModify()) {
            if (!topicsAdmin.topicExists(t.name)) {
                createTopics.add(t)
            }
        }

        logger.info("Creating the following topics that did not exist: {}", createTopics)
        topicsAdmin.createOrChangeTopic(createTopics)
        adminClient.close()
    }

    fun close() {
        streams.close()
        topicsAdmin.close()
    }
}