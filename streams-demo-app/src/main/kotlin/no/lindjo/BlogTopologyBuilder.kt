package no.lindjo

import no.lindjo.config.KafkaInputTopics
import no.lindjo.config.KafkaOutputTopics
import no.lindjo.model.ListDto
import no.lindjo.model.MailDto
import no.lindjo.model.PostDto
import no.lindjo.model.SubscriptionDto
import no.lindjo.model.UserDto
import no.lindjo.model.UserListPostInternal
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.serialization.Serdes.LongSerde
import org.apache.kafka.common.serialization.Serdes.String
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Grouped
import org.apache.kafka.streams.kstream.Joined
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.KTable
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.kstream.Produced
import org.slf4j.LoggerFactory
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer
import org.springframework.stereotype.Service
import java.util.*

@Service
class BlogTopologyBuilder(
        private val kafkaInputTopics: KafkaInputTopics,
        private val kafkaOutputTopics: KafkaOutputTopics
) {
    private val logger = LoggerFactory.getLogger(BlogTopologyBuilder::class.java)

    val longSerde: Serde<Long> = LongSerde()
    var stringSerde: Serde<String> = String()

    var userSerde: Serde<UserDto> = Serdes.serdeFrom(JsonSerializer(), JsonDeserializer(UserDto::class.java))
    var postSerde: Serde<PostDto> = Serdes.serdeFrom(JsonSerializer(), JsonDeserializer(PostDto::class.java))
    val listSerde: Serde<ListDto> = Serdes.serdeFrom(JsonSerializer(), JsonDeserializer(ListDto::class.java))
    val mailSerde: Serde<MailDto> = Serdes.serdeFrom(JsonSerializer(), JsonDeserializer(MailDto::class.java))
    var subscriptionSerde: Serde<SubscriptionDto> = Serdes.serdeFrom(JsonSerializer(), JsonDeserializer(SubscriptionDto::class.java))

    fun buildTopology(): Topology {
        val builder = StreamsBuilder()

        // 1. Create table of users (groupby)
        val userInfoTable = handleUserRegistration(builder)

        // 2. get number of subscriptions/subscribers (count)
        val subscriptionStream: KStream<String, SubscriptionDto> = handleSubscriptions(builder)

        // 3. get all subscribers (aggregate)
        val subscribersTable: KTable<String, ListDto> = aggregateSubscribers(subscriptionStream)

        // 4. send 'mail-event' to subscribers (join, flatMap)
        sendNotifications(builder, subscribersTable, userInfoTable)

        return builder.build()
    }

    private fun handleUserRegistration(builder: StreamsBuilder): KTable<String, UserDto>? {
        val userStream: KStream<String, UserDto> = builder.stream(
                kafkaInputTopics.users.name,
                Consumed.with(stringSerde, userSerde))

        userStream
                .selectKey { _, (username) -> username }
                .peek { k, v -> logger.info("Registered new user: {} - {}", k, v) }
                .to(kafkaOutputTopics.userInfo.name, Produced.with(stringSerde, userSerde))

        return builder.table(kafkaOutputTopics.userInfo.name, Consumed.with(stringSerde, userSerde))
    }

    private fun handleSubscriptions(builder: StreamsBuilder): KStream<String, SubscriptionDto> {
        val subscriptionStream: KStream<String, SubscriptionDto> = builder.stream(
                kafkaInputTopics.subscriptions.name,
                Consumed.with(stringSerde, subscriptionSerde))

        // 2a Subscription count
        subscriptionStream
                .selectKey { _, (username) -> username }
                .groupByKey(Grouped.with(stringSerde, subscriptionSerde))
                .count()
                .toStream()
                .peek { k, v -> logger.info("{} has {} subscription(s)", k, v) }
                .to(kafkaOutputTopics.userSubscriptionCount.name, Produced.with(stringSerde, longSerde))

        // 2b Subscriber count
        subscriptionStream
                .peek { k, v -> logger.info("{} -- {}", k, v) }
                .groupBy ({ k, v: SubscriptionDto -> v.subscribeTo }, Grouped.with(stringSerde, subscriptionSerde))
                .count()
                .toStream()
                .peek { k, v -> logger.info("{} has {} subscriber(s)", k, v) }
                .to(kafkaOutputTopics.userSubscribersCount.name, Produced.with(stringSerde, longSerde))

        return subscriptionStream
    }

    private fun aggregateSubscribers(subscriptionStream: KStream<String, SubscriptionDto>): KTable<String, ListDto> {
        val subscribersTable: KTable<String, ListDto> = subscriptionStream
                .selectKey { _, v -> v.subscribeTo }
                .groupByKey(Grouped.with(stringSerde, subscriptionSerde))
                .aggregate({ ListDto() },
                        { _, v, subscriptions ->
                            subscriptions.strings.add(v.username)
                            subscriptions
                        }, Materialized.with(stringSerde, listSerde))

        subscribersTable.toStream()
                .peek { k: String?, v: ListDto? -> logger.info("{} has the following subscribers {}", k, v) }

        return subscribersTable
    }

    private fun sendNotifications(builder: StreamsBuilder, subscribersTable: KTable<String, ListDto>, userInfoTable: KTable<String, UserDto>?) {
        val postStream: KStream<String, PostDto> = builder.stream(kafkaInputTopics.posts.name, Consumed.with(stringSerde, postSerde))

        postStream
                .selectKey { _, (_, username) -> username }
                .join(subscribersTable, { post: PostDto, subscribers: ListDto -> UserListPostInternal(post, subscribers) }, Joined.with(stringSerde, postSerde, listSerde))
                .flatMap { _, v ->
                    val keyValueList: MutableList<KeyValue<String, PostDto>> = ArrayList()
                    for (userName in v.userList.strings) {
                        keyValueList.add(KeyValue(userName, v.post))
                    }
                    keyValueList
                }
                .join(userInfoTable, { post, user -> MailDto(user.mail, post.title) }, Joined.with(stringSerde, postSerde, userSerde))
                .peek { k, v -> logger.info("Mail: {} {}", k, v) }
                .to(kafkaOutputTopics.eventMails.name, Produced.with(stringSerde, mailSerde))
    }
}