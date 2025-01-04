package org.woo.orchestrator.config

import io.r2dbc.spi.ConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing
import org.springframework.r2dbc.connection.R2dbcTransactionManager
import org.springframework.transaction.ReactiveTransactionManager
import org.springframework.transaction.reactive.TransactionalOperator

@Configuration
@EnableR2dbcAuditing
class R2dbcConfig {
    @Bean
    fun transactionManager(connectionFactory: ConnectionFactory): ReactiveTransactionManager = R2dbcTransactionManager(connectionFactory)

    @Bean
    fun transactionalOperator(transactionManager: ReactiveTransactionManager): TransactionalOperator =
        TransactionalOperator.create(transactionManager)
}
