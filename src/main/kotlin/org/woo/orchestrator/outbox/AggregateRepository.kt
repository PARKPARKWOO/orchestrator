package org.woo.orchestrator.outbox

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux

interface AggregateRepository : ReactiveCrudRepository<Aggregate, Long> {
    fun findByStatus(status: String): Flux<Aggregate>
}
