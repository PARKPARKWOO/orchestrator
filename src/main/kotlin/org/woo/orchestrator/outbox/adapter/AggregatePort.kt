package org.woo.orchestrator.outbox.adapter

interface AggregatePort {
    suspend fun markAsSent(id: Long): Int
}
