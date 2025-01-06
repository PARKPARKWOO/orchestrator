package org.woo.orchestrator.outbox.usecase

import org.woo.orchestrator.constant.RecordOperation
import org.woo.orchestrator.outbox.Outbox

interface OutboxUseCase {
    suspend fun propagateEvent(
        outbox: Outbox,
        recordOperation: RecordOperation,
    )

    suspend fun findByAggregateId(id: Long): List<Outbox>
}
