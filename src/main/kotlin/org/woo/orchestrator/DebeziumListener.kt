package org.woo.orchestrator

import io.debezium.config.Configuration
import io.debezium.engine.ChangeEvent
import io.debezium.engine.DebeziumEngine
import io.debezium.engine.RecordChangeEvent
import io.debezium.engine.format.ChangeEventFormat
import io.debezium.engine.format.Json
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.springframework.stereotype.Component
import java.util.concurrent.Executor
import java.util.concurrent.Executors

@Component
class DebeziumListener(
    private final val debeziumConfig: Configuration,
) {
    private val executor: Executor = Executors.newSingleThreadExecutor()

    private val debeziumEngine: DebeziumEngine<ChangeEvent<String, String>> = DebeziumEngine.create(Json::class.java)
        .using(debeziumConfig.asProperties())
        .notifying(::handleEvent)
        .build()

    @PostConstruct
    fun start() {
        this.executor.execute(debeziumEngine)
    }

    @PreDestroy
    fun stop() {
        this.debeziumEngine.close()
    }

    private fun handleEvent(event: ChangeEvent<String, String>) {
        println(event)
        println()
    }
}