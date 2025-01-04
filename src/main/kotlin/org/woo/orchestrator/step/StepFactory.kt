package org.woo.orchestrator.step

import org.springframework.stereotype.Component
import org.woo.orchestrator.outbox.EventType

@Component
class StepFactory(
    val steps: List<SagaStepTemplate>,
) {
    suspend fun findStep(type: EventType): SagaStepTemplate = steps.find { it.eventType == type } ?: throw RuntimeException()
}
