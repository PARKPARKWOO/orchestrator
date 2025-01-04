package org.woo.orchestrator.outbox.adapter

import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.awaitRowsUpdated
import org.springframework.stereotype.Component
import org.woo.orchestrator.outbox.TransactionStatus

@Component
class AggregateAdapter(
    private val databaseClient: DatabaseClient,
) : AggregatePort {
    companion object {
        // column
        const val ID_COLUMN = "id"
        const val AGGREGATE_ID_COLUMN = "aggregateId"
        const val STATUS_COLUMN = "status"

        // bind
        const val STATUS_BIND = "status"
        const val ID_BIND = "id"
        const val CURRENT_STATUS_BIND = "currentStatus"
    }

    override suspend fun markAsSent(id: Long): Int =
        databaseClient
            .sql(
                "UPDATE aggregate set $STATUS_COLUMN = :$STATUS_BIND WHERE $ID_COLUMN = :$ID_BIND AND $STATUS_COLUMN = :$CURRENT_STATUS_BIND",
            ).bind(STATUS_BIND, TransactionStatus.IN_PROGRESS.name)
            .bind(ID_BIND, id)
            .bind(CURRENT_STATUS_BIND, TransactionStatus.PENDING.name)
            .fetch()
            .awaitRowsUpdated()
            .toInt()
}
