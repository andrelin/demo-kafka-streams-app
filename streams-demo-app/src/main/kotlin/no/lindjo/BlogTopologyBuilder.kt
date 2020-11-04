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
        val userStream: KStream<String, UserDto> = builder.stream(
                kafkaInputTopics.users.name,
                Consumed.with(stringSerde, userSerde))

        userStream
                .selectKey { _, (username) -> username }
                .peek {k, v -> logger.info("{} - {}", k, v)}
                .to(kafkaOutputTopics.userInfo.name, Produced.with(stringSerde, userSerde))

        val userInfoTable = builder.table(kafkaOutputTopics.userInfo.name, Consumed.with<String, UserDto>(stringSerde, userSerde))

        // 2. get number of subscriptions/subscribers (count)
        val subscriptionStream: KStream<String, SubscriptionDto> = builder.stream(
                kafkaInputTopics.subscriptions.name,
                Consumed.with(stringSerde, subscriptionSerde))

        subscriptionStream
                .selectKey { _, (username) -> username }
                .groupByKey(Grouped.with(stringSerde, subscriptionSerde))
                .count()
                .toStream()
                .peek {k, v -> logger.info("{} - {}", k, v)}
                .to(kafkaOutputTopics.userSubscriptionCount.name, Produced.with(stringSerde, longSerde))

        subscriptionStream
                .groupBy({ _, v: SubscriptionDto -> v.subscribesTo }, Grouped.with(stringSerde, subscriptionSerde))
                .count()
                .toStream()
                .peek {k, v -> logger.info("{} - {}", k, v)}
                .to(kafkaOutputTopics.userSubscriptionCount.name, Produced.with(stringSerde, longSerde))

        // 3. get all subscribers (aggregate)
        val subscribersTable: KTable<String, ListDto> = subscriptionStream
                .selectKey { _, v -> v.subscribesTo }
                .groupByKey(Grouped.with(stringSerde, subscriptionSerde))
                .aggregate({ ListDto() },
                        { _, v, subscriptions ->
                            subscriptions.strings.add(v.username)
                            subscriptions
                        }, Materialized.with(stringSerde, listSerde))

        subscribersTable.toStream()
                .peek { k: String?, v: ListDto? -> logger.info("peek: {} - {}", k, v) }

        // 4. send 'mail-event' to subscribers (join, flatMap)
        val postStream: KStream<String, PostDto> = builder.stream(kafkaInputTopics.posts.name, Consumed.with(stringSerde, postSerde))

        postStream
                .selectKey { _, (username) -> username }
                .join(subscribersTable, { k: PostDto, v: ListDto -> UserListPostInternal(k, v) }, Joined.with(stringSerde, postSerde, listSerde))
                .flatMap { _, v ->
                    val keyValueList: MutableList<KeyValue<String, PostDto>> = ArrayList()
                    for (userName in v.userList.strings) {
                        keyValueList.add(KeyValue(userName, v.post))
                    }
                    keyValueList
                }
                .join(userInfoTable, { post, user -> MailDto(user.mail, post.title) }, Joined.with(stringSerde, postSerde, userSerde))
                .to(kafkaOutputTopics.eventMails.name, Produced.with(stringSerde, mailSerde))

        return builder.build()
    }
}