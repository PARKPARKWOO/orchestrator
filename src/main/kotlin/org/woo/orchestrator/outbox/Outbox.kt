package org.woo.orchestrator.outbox

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table(name = "outbox")
class Outbox(
    @Id
    @Column("id")
    val id: Long,
    @Column("aggregate_id")
    val aggregateId: String,
    @Column("payload")
    val payload: String,
    @Column("event_type")
    val eventType: String,
    @Column("status")
    var status: String,
    @Column("created_at")
    val createdAt: LocalDateTime,
) {
    companion object {
        fun create(
            aggregateId: String,
            payload: String,
            eventType: EventType,
        ): Outbox =
            Outbox(
                id = 0L,
                aggregateId = aggregateId,
                payload = payload,
                eventType = eventType.name,
                status = TransactionStatus.PENDING.name,
                createdAt = LocalDateTime.now(),
            )
    }
}
