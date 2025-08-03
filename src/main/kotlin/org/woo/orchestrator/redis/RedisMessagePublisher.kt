package org.woo.orchestrator.redis

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Component
import org.woo.mapper.Jackson

@Component
class RedisMessagePublisher(
    private val redisTemplate: ReactiveRedisTemplate<String, Any>,
) {
    suspend fun <T> publish(
        topic: String,
        message: T,
    ) = coroutineScope {
        if (message != null) {
            redisTemplate.convertAndSend(topic, Jackson.writeValueAsString(message)).awaitSingle()
        }
    }
}
