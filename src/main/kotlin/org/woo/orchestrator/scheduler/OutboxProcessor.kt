package org.woo.orchestrator.scheduler

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component
import org.woo.orchestrator.constant.RecordOperation
import org.woo.orchestrator.outbox.Aggregate
import org.woo.orchestrator.outbox.usecase.AggregateUseCase
import org.woo.orchestrator.outbox.usecase.OutboxUseCase
import java.util.concurrent.Executors

@Component
class OutboxProcessor(
    val outboxUseCase: OutboxUseCase,
    val aggregateUseCase: AggregateUseCase,
) {
    private val handlerDispatcher = Executors.newSingleThreadScheduledExecutor().asCoroutineDispatcher()
    private val handlerScope = CoroutineScope(handlerDispatcher)
    private val updateDispatcher = Executors.newSingleThreadScheduledExecutor().asCoroutineDispatcher()
    private val updateScope = CoroutineScope(updateDispatcher)

    @PostConstruct
    fun process() {
        handlerScope.launch {
            while (true) {
                aggregateUseCase.fetchPendingAggregate().forEach { aggregate ->
                    consumeEvnet(aggregate)
                }
            }
        }
    }

    @PreDestroy
    fun shutdown() {
        handlerDispatcher.close()
        updateDispatcher.close()
    }

    suspend fun consumeEvnet(aggregate: Aggregate) {
        val outboxes = outboxUseCase.findByAggregateId(aggregate.id)
        outboxes.forEach { outbox ->
            updateScope.launch {
                //  실패시 DLQ 에 넣기
                outboxUseCase.propagateEvent(outbox, RecordOperation.valueOf(aggregate.recordOperation))
            }
        }
        updateScope.launch {
            aggregateUseCase.markAsSent(aggregate.id)
        }
    }
}
