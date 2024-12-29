package org.woo.orchestrator.outbox

import org.springframework.data.repository.reactive.ReactiveCrudRepository

interface OutboxRepository : ReactiveCrudRepository<Outbox, Long>