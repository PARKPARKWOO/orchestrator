package org.woo.orchestrator.outbox.usecase

import org.woo.orchestrator.outbox.Outbox

interface OutboxUseCase {
    suspend fun propagateEvent(outbox: Outbox)

    suspend fun findByAggregateId(id: Long): List<Outbox>
}
