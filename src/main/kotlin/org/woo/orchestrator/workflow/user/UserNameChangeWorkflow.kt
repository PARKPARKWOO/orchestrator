package org.woo.orchestrator.workflow.user

import org.springframework.stereotype.Component
import org.woo.orchestrator.outbox.EventType
import org.woo.orchestrator.outbox.Outbox
import org.woo.orchestrator.workflow.SagaWorkflowTemplate
import org.woo.orchestrator.workflow.WorkflowCommand
import org.woo.orchestrator.workflow.command.UpdateUserNameCommand

@Component
class UserNameChangeWorkflow : SagaWorkflowTemplate() {
    override val eventType: EventType = EventType.UPDATE_USER_NAME

    override suspend fun execute(command: WorkflowCommand) {
    }

    override suspend fun rollback(command: WorkflowCommand) {
    }

    override suspend fun generateCommand(outbox: Outbox): WorkflowCommand = UpdateUserNameCommand(userId = "123", name = "name")
}
