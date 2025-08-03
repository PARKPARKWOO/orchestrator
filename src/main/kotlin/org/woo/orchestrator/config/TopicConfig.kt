package org.woo.orchestrator.config

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder
import org.woo.event.api.ApiEventConstants

// TODO: terraform 으로 이관후 삭제
@Configuration
class TopicConfig(
    @Value("\${topic.incoming.chat.message}")
    val incomingChatMessage: String,
    @Value("\${topic.outgoing.chat.save.message}")
    val saveMessageTopic: String,
) {
    @Bean
    fun internalApiCallTopic(): NewTopic =
        TopicBuilder
            .name(ApiEventConstants.INTERNAL_API_CALL_TOPIC)
            .partitions(1)
            .replicas(1)
            .build()

    @Bean
    fun externalApiCallTopic(): NewTopic =
        TopicBuilder
            .name(ApiEventConstants.EXTERNAL_API_CALL_TOPIC)
            .partitions(1)
            .replicas(1)
            .build()

    @Bean
    fun messageTopic(): NewTopic =
        TopicBuilder
            .name(incomingChatMessage)
            .partitions(1)
            .replicas(1)
            .build()

    @Bean
    fun outgoingTopic(): NewTopic =
        TopicBuilder
            .name(saveMessageTopic)
            .partitions(1)
            .replicas(1)
            .build()
}
