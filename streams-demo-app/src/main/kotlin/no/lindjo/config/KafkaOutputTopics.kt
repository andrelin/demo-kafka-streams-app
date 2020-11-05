package no.lindjo.config

import no.lindjo.TopicConfiguration
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "kafka.topics.output")
class KafkaOutputTopics {
    final var eventMails: TopicConfiguration = TopicConfiguration()
    final var userInfo: TopicConfiguration = TopicConfiguration()
    final var userSubscriptionCount: TopicConfiguration = TopicConfiguration()
    final var userSubscribersCount: TopicConfiguration = TopicConfiguration()
}
