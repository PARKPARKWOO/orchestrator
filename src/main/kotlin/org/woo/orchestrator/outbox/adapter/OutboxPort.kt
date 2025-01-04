package org.woo.orchestrator.outbox.adapter

interface OutboxPort {
    suspend fun updateStatus(
        id: Long,
        previousStatus: String,
        currentStatus: String,
    ): Int
}
