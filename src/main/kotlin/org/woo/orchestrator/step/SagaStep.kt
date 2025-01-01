package org.woo.orchestrator.step

import org.woo.orchestrator.outbox.EventType
import org.woo.orchestrator.outbox.Outbox

// saga step 들을 관리하는 factory 를 만들어볼까?
interface SagaStep {
    abstract val eventType: EventType

    suspend fun execute(command: StepCommand)

    suspend fun rollback(command: StepCommand)

    suspend fun generateCommand(outbox: Outbox): StepCommand
}
