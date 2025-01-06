package org.woo.orchestrator.workflow

import org.woo.orchestrator.constant.RecordOperation
import org.woo.orchestrator.outbox.EventType
import org.woo.orchestrator.outbox.Outbox

// saga step 들을 관리하는 factory 를 만들어볼까?
interface SagaWorkflow {
    abstract val eventType: EventType

    suspend fun execute(command: WorkflowCommand)

    suspend fun rollback(command: WorkflowCommand)

    suspend fun generateCommand(
        outbox: Outbox,
        recordOperation: RecordOperation,
    ): WorkflowCommand
}
