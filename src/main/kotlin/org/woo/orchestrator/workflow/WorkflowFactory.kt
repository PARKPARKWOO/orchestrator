package org.woo.orchestrator.workflow

import org.springframework.stereotype.Component
import org.woo.orchestrator.outbox.EventType

@Component
class WorkflowFactory(
    val steps: List<SagaWorkflowTemplate>,
) {
    suspend fun findStep(type: EventType): SagaWorkflowTemplate = steps.find { it.eventType == type } ?: throw RuntimeException()
}
