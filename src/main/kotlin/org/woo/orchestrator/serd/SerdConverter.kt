package org.woo.orchestrator.serd

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serializer

class SerdConverter<T>(
    private val targetClass: Class<T>,
) : Serde<T> {
    private val mapper: ObjectMapper =
        ObjectMapper()
            .registerKotlinModule()

    override fun serializer(): Serializer<T> =
        Serializer { _, data ->
            if (data == null) {
                null
            } else {
                mapper.writeValueAsBytes(data)
            }
        }

    override fun deserializer(): Deserializer<T> =
        Deserializer { _, bytes ->
            if (bytes == null || bytes.isEmpty()) {
                null
            } else {
                mapper.readValue(bytes, targetClass)
            }
        }

    override fun configure(
        configs: MutableMap<String, *>?,
        isKey: Boolean,
    ) { /* no-op */ }

    override fun close() { /* no-op */ }
}
