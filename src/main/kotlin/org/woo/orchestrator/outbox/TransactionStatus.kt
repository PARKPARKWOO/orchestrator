package org.woo.orchestrator.outbox

enum class TransactionStatus {
    PENDING,
    COMPENSATION,
    DONE,
    COMMIT,
    IN_PROGRESS,
}
