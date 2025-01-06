package org.woo.orchestrator.outbox.usecase

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.stereotype.Component
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import org.woo.orchestrator.constant.RecordOperation
import org.woo.orchestrator.debezium.DomainEvent
import org.woo.orchestrator.outbox.Aggregate
import org.woo.orchestrator.outbox.AggregateRepository
import org.woo.orchestrator.outbox.EventType
import org.woo.orchestrator.outbox.Outbox
import org.woo.orchestrator.outbox.OutboxRepository
import org.woo.orchestrator.outbox.TransactionStatus.IN_PROGRESS
import org.woo.orchestrator.outbox.TransactionStatus.PENDING
import org.woo.orchestrator.outbox.adapter.AggregatePort
import org.woo.orchestrator.outbox.adapter.OutboxPort
import org.woo.orchestrator.workflow.WorkflowFactory

@Component
class OutboxFacade(
    val outboxRepository: OutboxRepository,
    val outboxPort: OutboxPort,
    val aggregatePort: AggregatePort,
    val aggregateRepository: AggregateRepository,
    val workflowFactory: WorkflowFactory,
    private val transactionalOperator: TransactionalOperator,
) : OutboxUseCase,
    AggregateUseCase {
    override suspend fun propagateEvent(
        outbox: Outbox,
        recordOperation: RecordOperation,
    ) {
        val step = workflowFactory.findStep(EventType.valueOf(outbox.eventType))
        step.propagateEvent(outbox, recordOperation)
        outboxPort.updateStatus(
            id = outbox.id,
            previousStatus = IN_PROGRESS.name,
            currentStatus = PENDING.name,
        )
    }

    override suspend fun findByAggregateId(id: Long): List<Outbox> = outboxRepository.findByAggregateId(id).asFlow().toList()

    override suspend fun fetchPendingAggregate(): List<Aggregate> =
        aggregateRepository
            .findByStatus(PENDING.name)
            .asFlow()
            .toList()

    override suspend fun markAsSent(id: Long) {
        aggregatePort.markAsSent(id)
    }

    override suspend fun saveOutbox(event: DomainEvent) {
        transactionalOperator.executeAndAwait {
            val createAggregate = Aggregate.create(event.eventType, event.recordOperation)
            val aggregate = aggregateRepository.save(createAggregate).awaitSingle()
            // workflow 에서 Outbox 만드는것 고려 지금은 1개 라서 불필요 하긴함 group id 로 분리해야 하는지?
            val outbox = Outbox.create(aggregateId = aggregate.id, payload = event.payload, eventType = event.eventType)
            outboxRepository.save(outbox).awaitSingle()
        }
    }
}
