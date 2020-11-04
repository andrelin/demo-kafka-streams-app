package no.lindjo.config

import no.lindjo.TopicConfiguration


class KafkaTestInputTopics : KafkaInputTopics() {
    init {
//        users = TopicConfiguration.builder().name("usersTopic").build()
//        posts = TopicConfiguration.builder().name("postsTopic").build()
//        subscriptions = TopicConfiguration.builder().name("subscriptionsTopic").build()
//

        users = TopicConfiguration(name = "usersTopic")
        posts = TopicConfiguration(name = "postsTopic")
        subscriptions = TopicConfiguration(name = "subscriptionsTopic")


    }
}