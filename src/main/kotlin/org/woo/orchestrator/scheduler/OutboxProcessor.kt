package org.woo.orchestrator.scheduler

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import org.springframework.stereotype.Component
import org.woo.orchestrator.factory.StepFactory
import org.woo.orchestrator.outbox.EventType
import org.woo.orchestrator.outbox.Outbox
import org.woo.orchestrator.outbox.OutboxRepository
import org.woo.orchestrator.outbox.TransactionStatus
import java.util.concurrent.Executors

@Component
class OutboxProcessor(
    val stepFactory: StepFactory,
    val outboxRepository: OutboxRepository,
) {
    private val handlerDispatcher = Executors.newSingleThreadScheduledExecutor().asCoroutineDispatcher()
    private val handlerScope = CoroutineScope(handlerDispatcher + SupervisorJob())

    @PostConstruct
    fun process() {
        handlerScope.launch {
            while (true) {
                val events = fetchPendingOutbox()
                events.forEach { event ->
                    launch {
                        consumeEvnet(event)
                    }
                }
                delay(10)
            }
        }
    }

    @PreDestroy
    fun shutdown() {
        handlerDispatcher.close()
    }

    suspend fun fetchPendingOutbox(): List<Outbox> =
        outboxRepository
            .findByStatus(TransactionStatus.PENDING.name)
            .asFlow()
            .toList()

    suspend fun consumeEvnet(outbox: Outbox) {
        val step = stepFactory.findStep(EventType.valueOf(outbox.eventType))
        step.execute(step.generateCommand(outbox))
    }

    suspend fun markAsProcessed(outbox: Outbox) {
    }

    suspend fun markAsFailed(outbox: Outbox) {
    }

    suspend fun deleteEvent(outbox: Outbox) {
    }
}
