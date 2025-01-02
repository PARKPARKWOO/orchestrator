package org.woo.orchestrator

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import org.woo.orchestrator.outbox.Aggregate
import org.woo.orchestrator.outbox.AggregateRepository
import org.woo.orchestrator.outbox.EventType
import org.woo.orchestrator.outbox.Outbox
import org.woo.orchestrator.outbox.OutboxRepository
import org.woo.orchestrator.outbox.TransactionStatus
import java.util.concurrent.Executors

@RestController
class TestController(
    val outboxRepository: OutboxRepository,
    val aggregateRepository: AggregateRepository,
) {
    private val executor = Executors.newFixedThreadPool(10).asCoroutineDispatcher()

    @PostMapping("/test")
    suspend fun init() {
        for (i in 0..100) {
            CoroutineScope(executor).launch {
                val aggregate = Aggregate(0L, EventType.UPDATE_USER_NAME.name, TransactionStatus.PENDING.name)
                val entity = aggregateRepository.save(aggregate).awaitSingle()
                val payload =
                    mapOf(
                        "" to "",
                    )
                val outbox =
                    Outbox.create(
                        aggregateId = entity.id,
                        payload = payload.toString(),
                        eventType = EventType.UPDATE_USER_NAME,
                    )
                outboxRepository.save(outbox).awaitSingle()
            }
        }
    }
}
