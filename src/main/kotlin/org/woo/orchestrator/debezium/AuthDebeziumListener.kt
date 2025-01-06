package org.woo.orchestrator.debezium

import io.debezium.config.Configuration
import io.debezium.engine.ChangeEvent
import io.debezium.engine.DebeziumEngine
import io.debezium.engine.format.Json
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import java.util.concurrent.Executor
import java.util.concurrent.Executors

@Component
class AuthDebeziumListener(
    @Qualifier("authConnector")
    private final val debeziumConfig: Configuration,
    val applicationEventPublisher: ApplicationEventPublisher,
) {
    private val executor: Executor = Executors.newSingleThreadExecutor()

    private val debeziumEngine: DebeziumEngine<ChangeEvent<String, String>> =
        DebeziumEngine
            .create(Json::class.java)
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
        val domainEvent = event.getPayload()
        applicationEventPublisher.publishEvent(domainEvent)
    }
}
