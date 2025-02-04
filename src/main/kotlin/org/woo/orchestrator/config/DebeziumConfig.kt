package org.woo.orchestrator.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.support.JacksonUtils
import org.woo.orchestrator.constant.DomainService.AUTH

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
    val kafkaHost: String,
    @Value("\${mysql.host}")
    val debeziumHost: String,
    @Value("\${mysql.driver}")
    val driver: String,
    @Value("\${mysql.port}")
    val port: Int,
    @Value("\${mysql.schema}")
    val debeziumSchema: String,
    @Value("\${spring.r2dbc.username}")
    val debeziumUser: String,
    @Value("\${spring.r2dbc.password}")
    val debeziumPassword: String,
    @Value("\${spring.profiles.active}")
    val profile: String,
) {
    companion object {
        private const val MYSQL_PORT = 3306
        private const val TOPIC_PREFIX = "cdc"
        private const val MYSQL_CONNECTOR = "io.debezium.connector.mysql.MySqlConnector"

        //       관리 해야하는 테이블들이 많아져 전파가 필요한 이벤트는 모두 outbox table 로 관리한다.
        private const val TARGET_TABLE = "outbox"

        private const val SCHEMA_HISTORY_TOPIC = "schema-history"

        // 중복되지 않는 고유한 값 이어야 한다.
        private const val DEBEZIUM_SERVER_ID_IN_MYSQL = "9999"
    }

    @Bean
    fun mapper(): ObjectMapper = JacksonUtils.enhancedObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)

    @Bean("authConnector")
    fun authConnector(): io.debezium.config.Configuration =
        io.debezium.config.Configuration
            .create()
            .with("name", "auth-mysql-connector")
            .with("connector.class", MYSQL_CONNECTOR)
            .with("offset.storage", "io.debezium.storage.jdbc.offset.JdbcOffsetBackingStore")
            .with("offset.storage.jdbc.url", "$driver$debeziumHost:$port/$debeziumSchema")
            .with("offset.storage.jdbc.user", debeziumUser)
            .with("offset.storage.jdbc.password", debeziumPassword)
            .with("offset.flush.interval.ms", "60000")
            .with("database.hostname", authMysqlHost)
            .with("database.port", MYSQL_PORT)
            .with("database.user", authMysqlUser)
            .with("database.password", authMysqlPassword)
            .with("database.include.list", AUTH.database)
            .with("include.schema.changes", "false")
            .with("database.server.id", DEBEZIUM_SERVER_ID_IN_MYSQL)
            .with("database.server.name", "auth-mysql-db-server")
            .with("database.history", "io.debezium.relational.history.FileDatabaseHistory")
            .with("database.history.file.filename", "/tmp/dbhistory.dat")
            .with("topic.prefix", TOPIC_PREFIX)
            .with("schema.history.internal.kafka.bootstrap.servers", kafkaHost)
            .with("schema.history.internal.kafka.topic", "$profile.${AUTH.database}.$SCHEMA_HISTORY_TOPIC")
            .with("table.include.list", "$${AUTH.database}.$TARGET_TABLE")
            .build()
}
