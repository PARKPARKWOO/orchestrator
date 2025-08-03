package org.woo.orchestrator.event

import jakarta.annotation.PreDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.woo.orchestrator.debezium.DomainEvent
import org.woo.orchestrator.outbox.usecase.AggregateUseCase
import org.woo.orchestrator.redis.RedisMessagePublisher
import java.util.concurrent.Executors

@Component
class SpringEventListener(
    private val aggregateUseCase: AggregateUseCase,
    private val redisMessagePublisher: RedisMessagePublisher,
) {
    companion object {
        private const val CIRCUIT_BREAKER_TOPIC_PREFIX = "circuit:grpc:"
    }

    private val domainEventDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val domainEventCoroutineScope = CoroutineScope(domainEventDispatcher)

    private val circuitBreakerDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val circuitBreakerCoroutineScope = CoroutineScope(circuitBreakerDispatcher)

    @PreDestroy
    fun destroy() {
        domainEventCoroutineScope.cancel()
        circuitBreakerCoroutineScope.cancel()
    }

    @EventListener
    fun listenDomainEvent(event: DomainEvent) {
        domainEventCoroutineScope.launch {
            aggregateUseCase.saveOutbox(event)
        }
    }

    @EventListener
    fun listenCircuitBreakerEvent(event: CircuitBreakerEvent) {
        circuitBreakerCoroutineScope.launch {
            redisMessagePublisher.publish(CIRCUIT_BREAKER_TOPIC_PREFIX + event.serviceName, event.isOpen)
        }
    }
}
