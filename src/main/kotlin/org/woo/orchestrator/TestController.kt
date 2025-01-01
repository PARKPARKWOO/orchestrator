package org.woo.orchestrator

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.awaitSingle
import org.apache.kafka.common.protocol.types.Field.UUID
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import org.woo.orchestrator.outbox.EventType
import org.woo.orchestrator.outbox.Outbox
import org.woo.orchestrator.outbox.OutboxRepository
import java.util.concurrent.Executors

@RestController
class TestController(
    val outboxRepository: OutboxRepository,
) {
    private val executor = Executors.newFixedThreadPool(10).asCoroutineDispatcher()

    @PostMapping("/test")
    suspend fun init() {
        for (i in 0..100) {
            CoroutineScope(executor).launch {
                val payload =
                    mapOf(
                        "" to "",
                    )
                val outbox =
                    Outbox.create(
                        aggregateId =
                            java.util.UUID
                                .randomUUID()
                                .toString(),
                        aggregateType = EventType.UPDATE_USER_NAME.name,
                        payload = payload.toString(),
                        eventType = EventType.UPDATE_USER_NAME,
                    )
                outboxRepository.save(outbox).awaitSingle()
            }
        }
    }
}
