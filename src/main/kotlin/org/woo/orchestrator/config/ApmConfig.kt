package org.woo.orchestrator.config

import brave.Tracer
import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.web.server.WebFilter
import org.woo.apm.log.config.TracingConfig
import org.woo.apm.pyroscope.EnablePyroscope
import reactor.core.publisher.Hooks

@EnablePyroscope
@Configuration
@Import(TracingConfig::class)
class ApmConfig {
    @Bean
    fun trace(tracer: Tracer): WebFilter = TracingConfig.create(tracer)

    @PostConstruct
    fun init() {
        Hooks.enableAutomaticContextPropagation()
    }
}
