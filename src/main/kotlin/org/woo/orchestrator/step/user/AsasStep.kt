package org.woo.orchestrator.step.user

import org.woo.orchestrator.outbox.EventType
import org.woo.orchestrator.outbox.Outbox
import org.woo.orchestrator.step.SagaStep
import org.woo.orchestrator.step.StepCommand

class AsasStep : SagaStep {
    override val eventType: EventType = EventType.UPDATE_USER_NAME

    override suspend fun execute(command: StepCommand) {
        TODO("Not yet implemented")
    }

    override suspend fun rollback(command: StepCommand) {
    }

    override suspend fun generateCommand(outbox: Outbox): StepCommand {
        TODO("Not yet implemented")
    }
}
