package org.woo.orchestrator.outbox

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.woo.orchestrator.constant.RecordOperation

@Table(name = "aggregate")
class Aggregate(
    @Id
    @Column("id")
    val id: Long,
    @Column("type")
    val type: String,
    @Column("status")
    val status: String,
    @Column("record_operation")
    val recordOperation: String,
) {
    companion object {
        fun create(
            type: EventType,
            recordOperation: RecordOperation,
        ): Aggregate =
            Aggregate(
                id = 0L,
                type = type.name,
                status = TransactionStatus.PENDING.name,
                recordOperation = recordOperation.name,
            )
    }

    @Transient
    val outboxes: MutableList<Outbox> = mutableListOf()

    suspend fun setOutbox(outboxes: List<Outbox>) {
        this.outboxes.addAll(outboxes)
    }
}
