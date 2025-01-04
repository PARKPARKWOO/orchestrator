package org.woo.orchestrator.scheduler

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import org.springframework.stereotype.Component
import org.woo.orchestrator.factory.StepFactory
import org.woo.orchestrator.outbox.Aggregate
import org.woo.orchestrator.outbox.AggregateRepository
import org.woo.orchestrator.outbox.Outbox
import org.woo.orchestrator.outbox.OutboxRepository
import org.woo.orchestrator.outbox.TransactionStatus
import org.woo.orchestrator.outbox.adapter.AggregatePort
import org.woo.orchestrator.outbox.adapter.OutboxPort
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
        // FIXME: 여기 exception 발생함
//        val step = stepFactory.findStep(EventType.valueOf(aggregate.type))
//        step.execute(step.generateCommand(outbox))
        aggregate.outboxes.forEach { outbox ->
            updateScope.launch {
                outboxPort.markAsSent(outbox.id)
            }
        }
    }

    suspend fun markAsFailed(outbox: Outbox) {
    }

    suspend fun deleteEvent(outbox: Outbox) {
    }
}
