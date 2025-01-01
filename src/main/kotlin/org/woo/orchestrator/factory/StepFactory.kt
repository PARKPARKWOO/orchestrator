package org.woo.orchestrator.factory

import org.springframework.stereotype.Component
import org.woo.orchestrator.outbox.EventType
import org.woo.orchestrator.step.SagaStep

@Component
class StepFactory(
    val steps: List<SagaStep>,
) {
    suspend fun findStep(type: EventType): SagaStep = steps.find { it.eventType == type } ?: throw RuntimeException()
}
