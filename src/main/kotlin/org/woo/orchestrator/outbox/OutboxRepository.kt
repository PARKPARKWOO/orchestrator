package org.woo.orchestrator.outbox

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux

interface OutboxRepository : ReactiveCrudRepository<Outbox, Long> {
    fun findByStatus(status: String): Flux<Outbox>
}