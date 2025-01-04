package org.woo.orchestrator.workflow.command

import org.woo.orchestrator.workflow.WorkflowCommand

data class UpdateUserNameCommand(
    val userId: String,
    val name: String,
) : WorkflowCommand
