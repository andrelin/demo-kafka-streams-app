package no.lindjo.config

import no.lindjo.TopicConfiguration
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.stereotype.Component

@ConstructorBinding
@Component
@ConfigurationProperties(prefix = "kafka.topics.input")
class KafkaInputTopics {
    var users: TopicConfiguration = TopicConfiguration()
    var posts: TopicConfiguration = TopicConfiguration()
    var subscriptions: TopicConfiguration = TopicConfiguration()
}
