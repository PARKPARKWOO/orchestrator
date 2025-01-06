package org.woo.orchestrator.outbox.usecase

import org.woo.orchestrator.debezium.DomainEvent
import org.woo.orchestrator.outbox.Aggregate

interface AggregateUseCase {
    suspend fun fetchPendingAggregate(): List<Aggregate>

    suspend fun markAsSent(id: Long)

    suspend fun saveOutbox(event: DomainEvent)
}
