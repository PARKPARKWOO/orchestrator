package org.woo.orchestrator.kafka

import jakarta.annotation.PostConstruct
import kotlinx.coroutines.runBlocking
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Produced
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.woo.orchestrator.redis.RedisMessagePublisher
import org.woo.orchestrator.serd.SerdConverter

@Component
class ChatMessageStreams(
    private val builder: StreamsBuilder,
    @Value("\${topic.incoming.chat.message}")
    private val incomingTopic: String,
    @Value("\${topic.outgoing.chat.save.message}")
    private val outgoingTopic: String,
    private val redisMessagePublisher: RedisMessagePublisher,
) {
    companion object {
        private const val MIRROR_VIEW_SERVICE_NAME = "dev-mirror-view"
    }

    private val serd: SerdConverter<SubscribeMessage> = SerdConverter(SubscribeMessage::class.java)

    @PostConstruct
    fun streams() {
        val stream = builder.stream<String, SubscribeMessage>(incomingTopic, Consumed.with(Serdes.String(), serd))
        stream
            .peek { _, v ->
                runBlocking {
                    redisMessagePublisher.publish("$MIRROR_VIEW_SERVICE_NAME:${v.roomId}", v)
                }
            }.to(
                outgoingTopic,
                Produced.with(Serdes.String(), serd),
            )
    }
}

data class SubscribeMessage(
    val sender: String,
    val payload: String = "",
    val messageType: String,
    val roomId: String,
)
