package org.woo.orchestrator.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DebeziumConfig(
    @Value("\${consume.auth.database.host}")
    val authMysqlHost: String,
    @Value("\${consume.auth.database.user}")
    val authMysqlUser: String,
    @Value("\${consume.auth.database.password}")
    val authMysqlPassword: String,
//    현재는 단일 인스턴스로 적용한다.
    @Value("\${consume.kafka.bootstrap.server}")
    val kafkaHost: String
) {
    companion object {
        private const val MYSQL_PORT = 3306
        private const val AUTH_SCHEMA = "auth"
        private const val TOPIC_PREFIX = "cdc"
    }

    @Bean
    fun connector(): io.debezium.config.Configuration {
        return io.debezium.config.Configuration.create()
            .with("name", "auth-mysql-connector")
            .with("connector.class", "io.debezium.connector.mysql.MySqlConnector")
            .with("offset.storage", "org.apache.kafka.connect.storage.FileOffsetBackingStore")
            .with("offset.storage.file.filename", "/tmp/offsets.dat")
            .with("offset.flush.interval.ms", "60000")
            .with("database.hostname", authMysqlHost)
            .with("database.port", MYSQL_PORT)
            .with("database.user", authMysqlUser)
            .with("database.password", authMysqlPassword)
            .with("database.include.list", AUTH_SCHEMA)
            .with("include.schema.changes", "false")
            .with("database.server.id", "1")
//            .with("database.server.name", "customer-mysql-db-server")
            .with("database.history", "io.debezium.relational.history.FileDatabaseHistory")
            .with("database.history.file.filename", "/tmp/dbhistory.dat")
            .with("topic.prefix", TOPIC_PREFIX)
            .with("schema.history.internal.kafka.bootstrap.servers", kafkaHost)
            .with("schema.history.internal.kafka.topic", "why")
            .build();
    }
}