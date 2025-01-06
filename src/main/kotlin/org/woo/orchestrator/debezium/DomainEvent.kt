package org.woo.orchestrator.debezium

import org.woo.orchestrator.constant.RecordOperation
import org.woo.orchestrator.outbox.EventType
import java.time.LocalDateTime

data class DomainEvent(
    val id: Long,
    val payload: String,
    val eventType: EventType,
//    @Column("status")
//    var status: String,
    val recordOperation: RecordOperation,
    val createdAt: LocalDateTime,
)
