package org.woo.orchestrator.outbox.usecase

import org.woo.orchestrator.outbox.Aggregate

interface AggregateUseCase {
    suspend fun fetchPendingAggregate(): List<Aggregate>

    suspend fun markAsSent(id: Long)
}
