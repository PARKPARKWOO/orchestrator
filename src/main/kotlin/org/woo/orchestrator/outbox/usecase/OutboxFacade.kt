package org.woo.orchestrator.outbox.usecase

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import org.springframework.stereotype.Component
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
) : OutboxUseCase,
    AggregateUseCase {
    override suspend fun propagateEvent(outbox: Outbox) {
        val step = workflowFactory.findStep(EventType.valueOf(outbox.eventType))
        step.propagateEvent(outbox)
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
}
