package org.woo.orchestrator.workflow

import org.woo.orchestrator.outbox.Outbox
import org.woo.orchestrator.outbox.TransactionStatus

abstract class SagaWorkflowTemplate : SagaWorkflow {
    abstract override suspend fun generateCommand(outbox: Outbox): WorkflowCommand

    abstract override suspend fun execute(command: WorkflowCommand)

    abstract override suspend fun rollback(command: WorkflowCommand)

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
