package org.woo.orchestrator.scheduler

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import org.springframework.stereotype.Component
import org.woo.orchestrator.outbox.Aggregate
import org.woo.orchestrator.outbox.AggregateRepository
import org.woo.orchestrator.outbox.EventType
import org.woo.orchestrator.outbox.OutboxRepository
import org.woo.orchestrator.outbox.TransactionStatus
import org.woo.orchestrator.outbox.adapter.AggregatePort
import org.woo.orchestrator.outbox.adapter.OutboxPort
import org.woo.orchestrator.step.StepFactory
import java.util.concurrent.Executors

@Component
class OutboxProcessor(
    val stepFactory: StepFactory,
    val outboxRepository: OutboxRepository,
    val outboxPort: OutboxPort,
    val aggregatePort: AggregatePort,
    val aggregateRepository: AggregateRepository,
) {
    private val handlerDispatcher = Executors.newSingleThreadScheduledExecutor().asCoroutineDispatcher()
    private val handlerScope = CoroutineScope(handlerDispatcher)
    private val updateThread = Executors.newSingleThreadScheduledExecutor().asCoroutineDispatcher()
    private val updateScope = CoroutineScope(updateThread)

    @PostConstruct
    fun process() {
        handlerScope.launch {
            while (true) {
                fetchPendingAggregate().forEach { aggregate ->
                    consumeEvnet(aggregate)
                }
            }
        }
    }

    @PreDestroy
    fun shutdown() {
        handlerDispatcher.close()
        updateThread.close()
    }

    suspend fun fetchPendingAggregate(): List<Aggregate> =
        aggregateRepository
            .findByStatus(TransactionStatus.PENDING.name)
            .asFlow()
            .toList()

    suspend fun consumeEvnet(aggregate: Aggregate) {
        aggregatePort.markAsSent(aggregate.id)
        val outboxes = outboxRepository.findByAggregateId(aggregate.id).asFlow()
        aggregate.setOutbox(outboxes.toList())
        val step = stepFactory.findStep(EventType.valueOf(aggregate.type))
        aggregate.outboxes.forEach { outbox ->
            updateScope.launch {
                step.propagateEvent(outbox)
                outboxPort.markAsSent(outbox.id)
            }
        }
    }
}
