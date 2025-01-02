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
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.stereotype.Component
import org.woo.orchestrator.factory.StepFactory
import org.woo.orchestrator.outbox.Aggregate
import org.woo.orchestrator.outbox.AggregateRepository
import org.woo.orchestrator.outbox.EventType
import org.woo.orchestrator.outbox.Outbox
import org.woo.orchestrator.outbox.OutboxRepository
import org.woo.orchestrator.outbox.TransactionStatus
import java.util.concurrent.Executors

@Component
class OutboxProcessor(
    val stepFactory: StepFactory,
    val outboxRepository: OutboxRepository,
    val aggregateRepository: AggregateRepository,
) {
    private val handlerDispatcher = Executors.newSingleThreadScheduledExecutor().asCoroutineDispatcher()
    private val handlerScope = CoroutineScope(handlerDispatcher + SupervisorJob())
    private val updateThread = Executors.newSingleThreadScheduledExecutor().asCoroutineDispatcher()
    private val updateScope = CoroutineScope(updateThread)

    @PostConstruct
    fun process() {
        handlerScope.launch {
            while (true) {
                runCatching {
                    val aggregates = fetchPendingAggregate()
                    aggregates.forEach { aggregate ->
                        launch {
                            consumeEvnet(aggregate)
                        }
                    }
                    delay(10)
                }
            }
        }
    }

    @PreDestroy
    fun shutdown() {
        handlerDispatcher.close()
    }

    suspend fun fetchPendingAggregate(): List<Aggregate> =
        aggregateRepository
            .findByStatus(TransactionStatus.PENDING.name)
            .asFlow()
            .toList()

    suspend fun consumeEvnet(aggregate: Aggregate) {
        val outboxes = outboxRepository.findByAggregateId(aggregate.id).asFlow()
        aggregate.setOutbox(outboxes.toList())
        val step = stepFactory.findStep(EventType.valueOf(aggregate.type))
//        step.execute(step.generateCommand(outbox))
        aggregate.outboxes.forEach { outbox ->
            updateScope.launch {
                outbox.send()
                outboxRepository.save(outbox).awaitSingle()
            }
        }
    }

    suspend fun markAsFailed(outbox: Outbox) {
    }

    suspend fun deleteEvent(outbox: Outbox) {
    }
}
