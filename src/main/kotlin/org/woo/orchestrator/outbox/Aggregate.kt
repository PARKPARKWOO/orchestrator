package org.woo.orchestrator.outbox

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table(name = "aggregate")
class Aggregate(
    @Id
    @Column("id")
    val id: Long,
    @Column("type")
    val type: String,
    @Column("status")
    val status: String,
) {
    @Transient
    val outboxes: MutableList<Outbox> = mutableListOf()

    suspend fun setOutbox(outboxes: List<Outbox>) {
        this.outboxes.addAll(outboxes)
    }
}
