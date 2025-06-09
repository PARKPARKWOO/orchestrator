package org.woo.orchestrator.scheduler

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.stereotype.Component
import org.woo.orchestrator.constant.RecordOperation
import org.woo.orchestrator.outbox.Aggregate
import org.woo.orchestrator.outbox.usecase.AggregateUseCase
import org.woo.orchestrator.outbox.usecase.OutboxUseCase
import java.util.concurrent.ScheduledExecutorService

@Component
class OutboxProcessor(
    val outboxUseCase: OutboxUseCase,
    val aggregateUseCase: AggregateUseCase,
    @Qualifier("outbox-coordinator")
    val outboxCoordinatorThread: ScheduledExecutorService,
    @Qualifier("outbox-worker")
    val outboxWorkerThread: ThreadPoolTaskExecutor,
) {
    private val coordinatorDispatcher by lazy { outboxCoordinatorThread.asCoroutineDispatcher() }
    private val coordinatorCoroutineScope = CoroutineScope(coordinatorDispatcher)
    private val workerDispatcher by lazy { outboxWorkerThread.asCoroutineDispatcher() }
    private val workerCoroutineScope = CoroutineScope(workerDispatcher)

    @PostConstruct
    fun process() {
        coordinatorCoroutineScope.launch {
            while (true) {
                aggregateUseCase.fetchPendingAggregate().forEach { aggregate ->
                    consumeEvnet(aggregate)
                    delay(100)
                }
            }
        }
    }

    @PreDestroy
    fun shutdown() {
        workerCoroutineScope.cancel()
        coordinatorCoroutineScope.cancel()
    }

    suspend fun consumeEvnet(aggregate: Aggregate) {
        val outboxes = outboxUseCase.findByAggregateId(aggregate.id)
        // TODO: Exception 처리
        outboxes
            .map { outbox ->
                workerCoroutineScope.async {
                    outboxUseCase.propagateEvent(outbox, RecordOperation.valueOf(aggregate.recordOperation))
                }
            }.awaitAll()

        aggregateUseCase.markAsSent(aggregate.id)
    }
}
