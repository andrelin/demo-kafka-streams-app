package no.lindjo

import no.lindjo.config.KafkaConfiguration
import no.lindjo.config.KafkaInputTopics
import no.lindjo.config.KafkaOutputTopics
import no.lindjo.config.KafkaTestConfigurationImpl
import no.lindjo.config.KafkaTestInputTopics
import no.lindjo.config.KafkaTestOutputTopics
import no.lindjo.model.UserDto
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.TestInputTopic
import org.apache.kafka.streams.TestOutputTopic
import org.apache.kafka.streams.TopologyTestDriver
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer
import java.time.Duration

class StreamTest {
    private val inputTopics: KafkaInputTopics = KafkaTestInputTopics()
    private val outputTopics: KafkaOutputTopics = KafkaTestOutputTopics()
    private val kafkaConfiguration: KafkaConfiguration = KafkaTestConfigurationImpl()

    private lateinit var testDriver: TopologyTestDriver
    private lateinit var userInput: TestInputTopic<String, UserDto>
    private lateinit var userInfoOutput: TestOutputTopic<String, UserDto>

    var userSerde: Serde<UserDto> = Serdes.serdeFrom(JsonSerializer(), JsonDeserializer(UserDto::class.java))

    @BeforeEach
    fun setup() {
        val topologyBuilder = BlogTopologyBuilder(inputTopics, outputTopics)

        // Set up Topology Test Driver
        testDriver = TopologyTestDriver(topologyBuilder.buildTopology(), kafkaConfiguration.getProperties())

        // Set up input topics writers
        userInput = testDriver.createInputTopic(inputTopics.users.name, Serdes.StringSerde().serializer(), userSerde.serializer())

        // Set up output topics readers
        userInfoOutput = testDriver.createOutputTopic(outputTopics.userInfo.name, Serdes.StringSerde().deserializer(), userSerde.deserializer())
    }

    @Test
    fun `Writing a new User results in that User with username as key`() {
        // Create Input
        val user = UserDto(username = "user", name = "name", mail = "mail")

        // Send Input
        userInput.pipeInput("", user)
        testDriver.advanceWallClockTime(Duration.ofSeconds(5))
        Assertions.assertThat(userInfoOutput.queueSize).isEqualTo(1)

        // Read Output
        val readRecord = userInfoOutput.readRecord()
        Assertions.assertThat(userInfoOutput.isEmpty)

        // Validate Entity
        val readUserDto = readRecord.value
        Assertions.assertThat(readRecord.key).isEqualTo(user.username)
        Assertions.assertThat(readUserDto).isEqualTo(user)
    }

}