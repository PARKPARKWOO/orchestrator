package org.woo.orchestrator.event

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.woo.orchestrator.debezium.DomainEvent
import org.woo.orchestrator.outbox.usecase.AggregateUseCase
import java.util.concurrent.Executors

@Component
class SpringEventListener(
    val aggregateUseCase: AggregateUseCase,
) {
    private val executor = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val scope = CoroutineScope(executor)

    @EventListener
    fun subscribe(event: DomainEvent) {
        scope.launch {
            aggregateUseCase.saveOutbox(event)
        }
    }
}
