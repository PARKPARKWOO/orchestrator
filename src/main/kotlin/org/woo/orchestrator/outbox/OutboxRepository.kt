package org.woo.orchestrator.outbox

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

interface OutboxRepository : ReactiveCrudRepository<Outbox, Long> {
    fun findByStatus(status: String): Flux<Outbox>

    fun findByAggregateId(aggregateId: Long): Flux<Outbox>
}

interface OutboxPort {
    suspend fun updateOutbox(outbox: Outbox)
}

@Repository
class OutboxAdapter : OutboxPort {
    override suspend fun updateOutbox(outbox: Outbox) {
        println()
    }
}
