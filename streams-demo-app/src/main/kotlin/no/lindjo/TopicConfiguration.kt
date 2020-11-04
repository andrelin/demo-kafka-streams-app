package no.lindjo

import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
data class TopicConfiguration(
        val name: String = "",
        val numReplicas: Short = 0,
        val numPartitions: Int = 0,
        val deleteTopic: Boolean = false,
        val configuration: HashMap<String, String> = HashMap()
)

