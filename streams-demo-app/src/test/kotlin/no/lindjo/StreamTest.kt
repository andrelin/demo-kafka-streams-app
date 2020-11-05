package no.lindjo

import no.lindjo.config.KafkaConfiguration
import no.lindjo.config.KafkaInputTopics
import no.lindjo.config.KafkaOutputTopics
import no.lindjo.config.KafkaTestConfigurationImpl
import no.lindjo.config.KafkaTestInputTopics
import no.lindjo.config.KafkaTestOutputTopics
import no.lindjo.model.MailDto
import no.lindjo.model.PostDto
import no.lindjo.model.SubscriptionDto
import no.lindjo.model.UserDto
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.TestInputTopic
import org.apache.kafka.streams.TestOutputTopic
import org.apache.kafka.streams.TopologyTestDriver
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer

class StreamTest {
    private val inputTopics: KafkaInputTopics = KafkaTestInputTopics()
    private val outputTopics: KafkaOutputTopics = KafkaTestOutputTopics()
    private val kafkaConfiguration: KafkaConfiguration = KafkaTestConfigurationImpl()

    private lateinit var testDriver: TopologyTestDriver

    private lateinit var userInput: TestInputTopic<String, UserDto>
    private lateinit var subscriptionInput: TestInputTopic<String, SubscriptionDto>
    private lateinit var postInput: TestInputTopic<String, PostDto>

    private lateinit var userInfoOutput: TestOutputTopic<String, UserDto>
    private lateinit var subscriptionCountOutput: TestOutputTopic<String, Long>
    private lateinit var subscriberCountOutput: TestOutputTopic<String, Long>
    private lateinit var notificationOutput: TestOutputTopic<String, MailDto>

    private var userSerde: Serde<UserDto> = Serdes.serdeFrom(JsonSerializer(), JsonDeserializer(UserDto::class.java))
    private var subscriptionSerde: Serde<SubscriptionDto> = Serdes.serdeFrom(JsonSerializer(), JsonDeserializer(SubscriptionDto::class.java))
    var postSerde: Serde<PostDto> = Serdes.serdeFrom(JsonSerializer(), JsonDeserializer(PostDto::class.java))
    var mailSerde: Serde<MailDto> = Serdes.serdeFrom(JsonSerializer(), JsonDeserializer(MailDto::class.java))

    @BeforeEach
    fun setup() {
        val topologyBuilder = BlogTopologyBuilder(inputTopics, outputTopics)

        // Set up Topology Test Driver
        testDriver = TopologyTestDriver(topologyBuilder.buildTopology(), kafkaConfiguration.getProperties())

        // Set up input topics writers
        userInput = testDriver.createInputTopic(inputTopics.users.name, Serdes.StringSerde().serializer(), userSerde.serializer())
        subscriptionInput = testDriver.createInputTopic(inputTopics.subscriptions.name, Serdes.StringSerde().serializer(), subscriptionSerde.serializer())
        postInput = testDriver.createInputTopic(inputTopics.posts.name, Serdes.StringSerde().serializer(), postSerde.serializer())

        // Set up output topics readers
        userInfoOutput = testDriver.createOutputTopic(outputTopics.userInfo.name, Serdes.StringSerde().deserializer(), userSerde.deserializer())
        subscriptionCountOutput = testDriver.createOutputTopic(outputTopics.userSubscriptionCount.name, Serdes.StringSerde().deserializer(), Serdes.Long().deserializer())
        subscriberCountOutput = testDriver.createOutputTopic(outputTopics.userSubscribersCount.name, Serdes.StringSerde().deserializer(), Serdes.Long().deserializer())
        notificationOutput = testDriver.createOutputTopic(outputTopics.eventMails.name, Serdes.StringSerde().deserializer(), mailSerde.deserializer())
    }

    @AfterEach
    fun close() {
        testDriver.close()
    }

    @Test
    fun `Registering a new User works`() {
        // Create Input
        val user = TestData.testUser("1")

        // Send Input
        userInput.pipeInput("", user)
        Assertions.assertThat(userInfoOutput.queueSize).isEqualTo(1)

        // Read Output
        val readRecord = userInfoOutput.readRecord()
        Assertions.assertThat(userInfoOutput.isEmpty)

        // Validate Entity
        val readUserDto = readRecord.value
        Assertions.assertThat(readRecord.key).isEqualTo(user.username)
        Assertions.assertThat(readUserDto).isEqualTo(user)
    }

    @Test
    fun `Registering a new Subscription`() {
        // Setup: Register a User
        val testUser1 = TestData.testUser("1")
        val testUser2 = TestData.testUser("2")
        userInput.pipeInput("", testUser1)
        userInput.pipeInput("", testUser2)

        // Create Input
        val subscriptionDto = SubscriptionDto(username = "user1", subscribesTo = "user2")

        // Send Input
        subscriptionInput.pipeInput("", subscriptionDto)
        Assertions.assertThat(subscriptionCountOutput.queueSize).isEqualTo(1)
        Assertions.assertThat(subscriberCountOutput.queueSize).isEqualTo(1)

        // Read Output
        val readRecordSubscriptionCount = subscriptionCountOutput.readRecord()
        Assertions.assertThat(subscriptionCountOutput.isEmpty)

        // Validate Entity
        Assertions.assertThat(readRecordSubscriptionCount.key).isEqualTo(testUser1.username)
        Assertions.assertThat(readRecordSubscriptionCount.value).isEqualTo(1)

        // Read Output
        val readRecordSubscriberCount = subscriberCountOutput.readRecord()
        Assertions.assertThat(subscriberCountOutput.isEmpty)

        // Validate Entity
        Assertions.assertThat(readRecordSubscriberCount.key).isEqualTo(testUser2.username)
        Assertions.assertThat(readRecordSubscriberCount.value).isEqualTo(1)
    }

    @Test
    fun `Sending a notification`() {
        // Setup: Register a User
        val testUser1 = TestData.testUser("1")
        val testUser2 = TestData.testUser("2")
        userInput.pipeInput("", testUser1)
        userInput.pipeInput("", testUser2)
        val subscriptionDto = SubscriptionDto(username = "user1", subscribesTo = "user2")
        subscriptionInput.pipeInput("", subscriptionDto)

        // Create Input
        val postDto = PostDto(title = "Super Interesting Post", username = testUser2.username)

        // Send Input
        postInput.pipeInput("", postDto)
        Assertions.assertThat(notificationOutput.queueSize).isEqualTo(1)

        // Read Output
        val readRecord = notificationOutput.readRecord()
        Assertions.assertThat(notificationOutput.isEmpty)

        // Validate Entity
        val expectedMail = MailDto(testUser1.mail, postDto.title)
        val actualMail = readRecord.value
        Assertions.assertThat(readRecord.key).isEqualTo(testUser1.username)
        Assertions.assertThat(actualMail).isEqualTo(expectedMail)
    }
}