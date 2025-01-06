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
    val executor = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    @EventListener
    fun subscribe(event: DomainEvent) {
        CoroutineScope(executor).launch {
            aggregateUseCase.saveOutbox(event)
            println("save event")
        }
    }
}
