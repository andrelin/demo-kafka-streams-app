package no.lindjo.util

//import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
//import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient
import no.lindjo.TopicConfiguration
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.Config
import org.apache.kafka.clients.admin.ConfigEntry
import org.apache.kafka.clients.admin.DeleteTopicsOptions
import org.apache.kafka.clients.admin.DescribeConfigsResult
import org.apache.kafka.clients.admin.ListTopicsResult
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.common.config.ConfigResource
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.stream.Collectors

class TopicsAdmin(
        private var adminClient: AdminClient
//        private var schemaRegistryClient: SchemaRegistryClient?
) {
    private val logger = LoggerFactory.getLogger(TopicsAdmin::class.java)

    var waitAfterDeleteSeconds = 20

//    fun deleteSchema(topicName: String?) {
//        if (schemaRegistryClient != null) {
//            val latestSchema: SchemaMetadata = schemaRegistryClient.getLatestSchemaMetadata(topicName)
//            if (latestSchema.getSchema() == null) {
//                logger.debug("Schema not found - no need to delete  %s", topicName)
//                return
//            }
//            logger.info("Deleting schema %s", topicName)
//            schemaRegistryClient.deleteSubject(topicName)
//        } else {
//            logger.error("Unable to delete from schemaregistry, schemaRegistryClient is null")
//        }
//    }

    fun listTopics(): ListTopicsResult {
        return adminClient.listTopics()
    }

    fun close() {
        adminClient.close()
    }

    @Throws(ExecutionException::class, InterruptedException::class)
    fun createOrChangeTopic(topicConfiguration: List<TopicConfiguration>) {
        deleteTopicsIfneeded(topicConfiguration)
        val existingTopics = createNewTopicsIfneeded(topicConfiguration)
        val existingTopicsConfigsResource: Collection<ConfigResource> = existingTopics.stream().map { topic: TopicConfiguration -> ConfigResource(ConfigResource.Type.TOPIC, topic.name) }.collect(Collectors.toList())
        val configresults: DescribeConfigsResult = adminClient.describeConfigs(existingTopicsConfigsResource)
        val configResourceConfigMap: Map<ConfigResource, Config> = configresults.all().get()
        for ((cfg, config) in configResourceConfigMap) {
            val topicName = cfg.name()
            logger.info(String.format("Will check if config for topic %s needs to be changed...", topicName))
            val currentConfigSettings: Collection<ConfigEntry> = config.entries()
            val newTopicConfig: Optional<TopicConfiguration> = topicConfiguration
                    .stream().filter { topic: TopicConfiguration -> topic.name.toString() == topicName }.findFirst()
            if (!newTopicConfig.isPresent()) {
                logger.warn("No config found for topic $topicName")
                continue
            }
            var isDirty = false
            val configs: MutableMap<ConfigResource, Config> = HashMap()

            for ((key, value) in newTopicConfig.get().configuration.entries) {
                logger.info(String.format("Checking config values for topic %s key %s value %s ",
                        topicName, key, value))
                if (currentConfigSettings.stream().anyMatch { cf: ConfigEntry -> cf.name().equals(key) && !cf.value().equals(value) }
                        ||
                        currentConfigSettings.stream().noneMatch { cf: ConfigEntry -> cf.name().equals(key) }) {
                    isDirty = true
                    logger.info(java.lang.String.format("Found config setting that needs an update : topic %s key %s value %s",
                            topicName, key,
                            config.entries().toString()
                    ))
                }
            }
            if (isDirty) {
                // all configs that belong to this topic must be re-added....
                val newConfigEntries: MutableCollection<ConfigEntry> = ArrayList<ConfigEntry>()
                for ((key, value) in newTopicConfig.get().configuration.entries) {
                    newConfigEntries.add(ConfigEntry(key, value))
                }
//                val newConfig = Config(newConfigEntries)
//                configs.putIfAbsent(cfg, newConfig)
//                val alterResults = adminClient.incrementalAlterConfigs( configs)
//                try {
//                    alterResults.all().get()
//                } catch (e: Exception) {
//                    logger.warn("error altering topic $topicName", e)
//                }
            }
        }
    }

    @Throws(ExecutionException::class, InterruptedException::class)
    fun getConfig(topicName: String?): Set<Map.Entry<ConfigResource, Config>> {
        val existingTopicsConfigsResource: Collection<ConfigResource> = Arrays.asList(ConfigResource(ConfigResource.Type.TOPIC, topicName))
        val configresults: DescribeConfigsResult = adminClient.describeConfigs(existingTopicsConfigsResource)
        val configResourceConfigMap: Map<ConfigResource, Config> = configresults.all().get()
        return configResourceConfigMap.entries
    }

    /**
     * Will delete a topic if it exists.
     * If it does not exist, log an info message.
     */
    @JvmOverloads
    @Throws(ExecutionException::class, InterruptedException::class)
    fun deleteTopic(topicName: String?, verifyRetries: Int = 10) {
        logger.info("Delete a topic %s ", topicName)
        val deleteTopicsOptions = DeleteTopicsOptions()
        deleteTopicsOptions.timeoutMs(20000)
        try {
            val deleteResponse = adminClient.deleteTopics(Arrays.asList(topicName), deleteTopicsOptions)
            deleteResponse.all().get()
            logger.info("Sleep so Kafka can delete the topic, will sleep for %d seconds", waitAfterDeleteSeconds)
            var i: Short = 0
            while (topicExists(topicName) && i < verifyRetries) {
                Thread.sleep(waitAfterDeleteSeconds * 1000.toLong())
                if (!topicExists(topicName)) {
                    logger.info("Successfully deleted topic: %s", topicName)
                } else {
                    logger.warn("Was unable to confirm delete topic: %s, will wait and check again %d times", topicName, verifyRetries - i)
                }
                i++
            }
        } catch (e: ExecutionException) {
            if (e.cause is UnknownTopicOrPartitionException) {
                logger.info("The topic %s does not exist, so cannot be deleted", topicName)
            } else {
                throw e
            }
        } catch (e: InterruptedException) {
            if (e.cause is UnknownTopicOrPartitionException) {
                logger.info("The topic %s does not exist, so cannot be deleted", topicName)
            } else {
                throw e
            }
        }
    }

    @Throws(ExecutionException::class, InterruptedException::class)
    fun deleteTopics(topicsToDelete: Collection<String?>) {
        val iterator = topicsToDelete.iterator()
        while (iterator.hasNext()) {
            deleteTopic(iterator.next())
        }
    }

    @Throws(ExecutionException::class, InterruptedException::class)
    private fun deleteTopicsIfneeded(topicConfiguration: List<TopicConfiguration>) {
        val topicsToDelete: Collection<String?> = topicConfiguration.stream().filter { topic: TopicConfiguration -> topic.deleteTopic }.map { it.name }
                .collect(Collectors.toList())
        if (topicsToDelete.isEmpty()) {
            logger.info("No topics need to be deleted")
            return
        }
        logger.info("Deleting some topics %s ", java.lang.String.join(",", topicsToDelete))
        deleteTopics(topicsToDelete)
    }

    @Throws(ExecutionException::class, InterruptedException::class)
    fun topicExists(topicName: String?): Boolean {
        val existingKafkaTopics = adminClient.listTopics()
        val existingTopicNames = existingKafkaTopics.names().get()
        logger.info("existing topics: {}", existingTopicNames)
        return existingTopicNames.contains(topicName)
    }

    @Throws(InterruptedException::class, ExecutionException::class)
    private fun createNewTopicsIfneeded(listOfTopics: List<TopicConfiguration>): List<TopicConfiguration> {
        val existingKafkaTopics = adminClient.listTopics()
        val existingTopicNames = existingKafkaTopics.names().get()
        val newTopics: Collection<NewTopic> = listOfTopics
                .stream().filter { top: TopicConfiguration -> !existingTopicNames.contains(top.name) }
                .map { topic: TopicConfiguration ->
                    NewTopic(
                            topic.name, topic.numPartitions, topic.numReplicas)
                            .configs(topic.configuration)
                }
                .collect(Collectors.toList())
        if (newTopics.isEmpty()) {
            logger.info("All topics exists already")
        } else {
            logger.info("Will create topics $newTopics")
        }
        if (!newTopics.isEmpty()) {
            adminClient.createTopics(newTopics).all().get()
        }
        return listOfTopics
                .stream()
                .filter { top: TopicConfiguration -> existingTopicNames.contains(top.name) }
                .collect(Collectors.toList())
    }
}
