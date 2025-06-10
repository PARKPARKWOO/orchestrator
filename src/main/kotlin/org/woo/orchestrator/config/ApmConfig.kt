package org.woo.orchestrator.config

import brave.Tracer
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import jakarta.annotation.PostConstruct
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Grouped
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.kstream.TimeWindows
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.kafka.config.KafkaStreamsConfiguration
import org.springframework.web.server.WebFilter
import org.woo.apm.log.config.TracingConfig
import org.woo.apm.pyroscope.EnablePyroscope
import org.woo.event.api.ApiEventConstants
import org.woo.event.api.ExternalApiCallEvent
import org.woo.orchestrator.kafka.InternalTopologyBuilder
import org.woo.orchestrator.serd.SerdConverter
import reactor.core.publisher.Hooks
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

@EnablePyroscope
@Configuration
@Import(TracingConfig::class)
class ApmConfig(
    private val meterRegistry: MeterRegistry,
    private val internalTopologyBuilder: InternalTopologyBuilder,
) {
    private val externalSerd: SerdConverter<ExternalApiCallEvent> = SerdConverter(ExternalApiCallEvent::class.java)
    private val externalFailureRateMap = ConcurrentHashMap<String, AtomicReference<Double>>()
    private val metricsSerd = SerdConverter(ApiCallStats::class.java)

    @Bean
    fun trace(tracer: Tracer): WebFilter = TracingConfig.create(tracer)

    @PostConstruct
    fun init() {
        Hooks.enableAutomaticContextPropagation()
    }

    @Bean
    fun internalTopology(
        streamsConfig: KafkaStreamsConfiguration,
        builder: StreamsBuilder,
    ): Topology = internalTopologyBuilder.build(builder)

    @Bean
    fun externalTopology(
        builder: StreamsBuilder,
        streamsConfig: KafkaStreamsConfiguration,
    ): Topology {
        // 내부/외부 토픽을 각각 Consumed.with 으로 읽어서 병합
        val externalStream =
            builder.stream(
                ApiEventConstants.EXTERNAL_API_CALL_TOPIC,
                Consumed.with(Serdes.String(), externalSerd),
            )

        externalStream
            .groupBy({ _, ev -> ev.baseUrl }, Grouped.with(Serdes.String(), externalSerd))
            .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(1)))
            .aggregate(
                // 초기값
                { ApiCallStats(0L, 0L) },
                { _, ev, agg ->
                    ApiCallStats(
                        total = agg.total + 1,
                        success = agg.success + if (ev.isSuccess) 1 else 0,
                    )
                },
                Materialized.with(Serdes.String(), metricsSerd),
            ).toStream()
            .foreach { windowKey, stats ->
                val baseUrl = windowKey.key()
                val total = stats.total.toDouble()
                val success = stats.success.toDouble()
                val failure = total - success

                meterRegistry
                    .counter("external_api_calls_total", listOf(Tag.of("uri", baseUrl)))
                    .increment(total)
                meterRegistry
                    .counter("external_api_calls_success", listOf(Tag.of("uri", baseUrl)))
                    .increment(success)
                meterRegistry
                    .counter("external_api_calls_failure", listOf(Tag.of("uri", baseUrl)))
                    .increment(failure)

                // 실패율 게이지용 AtomicReference
                val ref = externalFailureRateMap.computeIfAbsent(baseUrl) { AtomicReference(0.0) }
                val rate = if (stats.total > 0) failure / total else 0.0
                ref.set(rate)

                meterRegistry.gauge(
                    "external_api_failure_rate",
                    listOf(Tag.of("uri", baseUrl)),
                    ref,
                    AtomicReference<Double>::get,
                )
            }

        return builder.build()
    }
}

data class ApiCallStats(
    val total: Long = 0L,
    val success: Long = 0L,
)
