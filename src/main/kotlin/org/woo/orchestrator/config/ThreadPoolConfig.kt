package org.woo.orchestrator.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ThreadPoolExecutor

@Configuration
class ThreadPoolConfig {
    @Bean("outbox-coordinator")
    fun outboxCoordinatorThread(): ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()

    @Bean("outbox-worker")
    fun outboxWorkerThread() =
        ThreadPoolTaskExecutor().apply {
            corePoolSize = 2
            maxPoolSize = 4
            queueCapacity = 100
            keepAliveSeconds = 60

            setThreadNamePrefix("outbox-worker-")

            setWaitForTasksToCompleteOnShutdown(true)
            setAwaitTerminationSeconds(30)

            setRejectedExecutionHandler(ThreadPoolExecutor.CallerRunsPolicy())
            initialize()
        }
}
