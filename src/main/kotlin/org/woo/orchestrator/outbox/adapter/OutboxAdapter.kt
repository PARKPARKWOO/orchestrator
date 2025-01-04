package org.woo.orchestrator.outbox.adapter

import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.awaitRowsUpdated
import org.springframework.stereotype.Component

@Component
class OutboxAdapter(
    private val databaseClient: DatabaseClient,
) : OutboxPort {
    companion object {
        // column
        const val ID_COLUMN = "id"
        const val STATUS_COLUMN = "status"

        // bind
        const val STATUS_BIND = "status"
        const val ID_BIND = "id"
        const val CURRENT_STATUS_BIND = "currentStatus"
    }

    override suspend fun updateStatus(
        id: Long,
        previousStatus: String,
        currentStatus: String,
    ): Int =
        databaseClient
            .sql(
                "UPDATE outbox set $STATUS_COLUMN = :$STATUS_BIND WHERE $ID_COLUMN = :$ID_BIND AND $STATUS_COLUMN = :$CURRENT_STATUS_BIND",
            ).bind(STATUS_BIND, previousStatus)
            .bind(ID_BIND, id)
            .bind(CURRENT_STATUS_BIND, currentStatus)
            .fetch()
            .awaitRowsUpdated()
            .toInt()
}
