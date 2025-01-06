package org.woo.orchestrator.debezium

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import io.debezium.engine.ChangeEvent
import org.springframework.kafka.support.JacksonUtils

fun ChangeEvent<String, String>.getPayload(): DomainEvent {
    val eventValue = this.value()
    val mapper = JacksonUtils.enhancedObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
    val dto = mapper.readValue(eventValue, ChangeEventDto::class.java)
    val payload = mapper.writeValueAsString(dto.payload.after)
    return mapper.readValue(payload, DomainEvent::class.java)
}

data class ChangeEventDto(
    val schema: Schema,
    val payload: Payload,
)

data class Schema(
    val type: String,
    val fields: List<Field>,
    val optional: Boolean,
    val name: String,
    val version: Int,
)

data class Field(
    val type: String,
    val fields: List<Field>? = null,
    val optional: Boolean,
    val name: String? = null,
    val version: Int? = null,
    @JsonProperty("field")
    val fieldName: String,
)

data class Payload(
    val before: Data? = null,
    val after: Data?,
    val source: Source,
    val transaction: Transaction? = null,
    val op: String,
    @JsonProperty("ts_ms")
    val tsMs: Long,
    @JsonProperty("ts_us")
    val tsUs: Long,
    @JsonProperty("ts_ns")
    val tsNs: Long,
)

data class Data(
    val id: Long,
    val payload: String,
    @JsonProperty("event_type")
    val eventType: String,
    @JsonProperty("record_operation")
    val recordOperation: String,
    @JsonProperty("created_at")
    val createdAt: String,
)

data class Source(
    val version: String,
    val connector: String,
    val name: String,
    @JsonProperty("ts_ms")
    val tsMs: Long,
    val snapshot: String,
    val db: String,
    val sequence: String?,
    @JsonProperty("ts_us")
    val tsUs: Long,
    @JsonProperty("ts_ns")
    val tsNs: Long,
    val table: String,
    @JsonProperty("server_id")
    val serverId: Long,
    val gtid: String?,
    val file: String,
    val pos: Long,
    val row: Int,
    val thread: Long?,
    val query: String?,
)

data class Transaction(
    val id: String,
    @JsonProperty("total_order")
    val totalOrder: Long,
    @JsonProperty("data_collection_order")
    val dataCollectionOrder: Long,
)
