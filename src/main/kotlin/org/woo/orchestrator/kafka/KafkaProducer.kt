package org.woo.orchestrator.kafka

import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class KafkaProducer(
    val kafkaTemplate: KafkaTemplate<String, String>,
) {
    suspend fun send(
        topic: String,
        message: String,
    ) {
        kafkaTemplate.send(topic, message)
    }
}
