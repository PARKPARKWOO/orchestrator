package org.woo.orchestrator.config

import io.asyncer.r2dbc.mysql.MySqlConnectionConfiguration
import io.asyncer.r2dbc.mysql.MySqlConnectionFactory
import io.r2dbc.pool.ConnectionPool
import io.r2dbc.pool.ConnectionPoolConfiguration
import io.r2dbc.spi.ConnectionFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing
import org.springframework.r2dbc.connection.R2dbcTransactionManager
import org.springframework.transaction.ReactiveTransactionManager
import org.springframework.transaction.reactive.TransactionalOperator

@Configuration
@EnableR2dbcAuditing
class R2dbcConfig(
    @Value("\${mysql.host}")
    private val host: String,
    @Value("\${spring.r2dbc.username}")
    private val username: String,
    @Value("\${spring.r2dbc.password}")
    private val password: String,
) : AbstractR2dbcConfiguration() {
    companion object {
        const val MYSQL_CONNECTION_TIME_OUT_SECONDS = 600L
        const val CONNECTION_POOL_INITIAL_SIZE = 25
        const val CONNECTION_POOL_MIN_IDLE = 25
        const val CONNECTION_POOL_MAX_SIZE = 50
        const val CONNECTION_POOL_MAX_IDLE_MIN = 5L
        const val DATABASE_SCHEMA = "orchestrator"
    }

    @Bean
    fun transactionManager(connectionFactory: ConnectionFactory): ReactiveTransactionManager = R2dbcTransactionManager(connectionFactory)

    @Bean
    fun transactionalOperator(transactionManager: ReactiveTransactionManager): TransactionalOperator =
        TransactionalOperator.create(transactionManager)

    @Bean
    override fun connectionFactory(): ConnectionFactory {
        val mySqlConnectionFactory = MySqlConnectionFactory.from(mysqlConfig())
        val connectionPoolConfiguration = connectionPoolConfig(mySqlConnectionFactory)
        val connectionPool = ConnectionPool(connectionPoolConfiguration)
        connectionPool.warmup().block()
        return connectionPool
    }

    private fun connectionPoolConfig(mysqlConfig: MySqlConnectionFactory): ConnectionPoolConfiguration =
        ConnectionPoolConfiguration
            .builder()
            .connectionFactory(mysqlConfig)
            .initialSize(CONNECTION_POOL_MIN_IDLE)
            .minIdle(CONNECTION_POOL_MIN_IDLE)
            .maxSize(CONNECTION_POOL_MAX_SIZE)
            .maxIdleTime(java.time.Duration.ofMinutes(CONNECTION_POOL_MAX_IDLE_MIN))
            .build()

    private fun mysqlConfig() =
        MySqlConnectionConfiguration
            .builder()
            .host(host)
            .database(DATABASE_SCHEMA)
            .user(username)
            .password(password)
            .connectTimeout(java.time.Duration.ofSeconds(MYSQL_CONNECTION_TIME_OUT_SECONDS))
            .build()
}
