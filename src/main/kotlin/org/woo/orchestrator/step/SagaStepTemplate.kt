package org.woo.orchestrator.step

import org.woo.orchestrator.outbox.Outbox
import org.woo.orchestrator.outbox.TransactionStatus

abstract class SagaStepTemplate : SagaStep {
    abstract override suspend fun generateCommand(outbox: Outbox): StepCommand

    abstract override suspend fun execute(command: StepCommand)

    abstract override suspend fun rollback(command: StepCommand)

    suspend fun propagateEvent(outbox: Outbox) {
        val command = generateCommand(outbox)
        val transactionType: TransactionStatus = TransactionStatus.valueOf(outbox.status)
        when (transactionType) {
            TransactionStatus.PENDING -> execute(command)
            TransactionStatus.COMPENSATION -> rollback(command)
            else -> {}
        }
    }
}
