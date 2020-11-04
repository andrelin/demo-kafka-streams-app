package no.lindjo.config

import no.lindjo.TopicConfiguration

class KafkaTestOutputTopics : KafkaOutputTopics() {
    init {
        userInfo = TopicConfiguration(name = "userInfoTopic")
        userSubscriptionCount = TopicConfiguration(name = "userSubscriptionCountTopic")
        eventMails = TopicConfiguration(name = "eventMailsTopic")
    }
}