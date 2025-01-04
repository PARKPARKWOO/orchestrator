package org.woo.orchestrator.outbox.adapter

interface OutboxPort {
    suspend fun markAsSent(id: Long): Int
}
