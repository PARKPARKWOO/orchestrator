package org.woo.orchestrator.kafka

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Grouped
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.kstream.TimeWindows
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import org.woo.event.api.ApiEventConstants
import org.woo.event.api.InternalApiCallEvent
import org.woo.orchestrator.event.CircuitBreakerEvent
import org.woo.orchestrator.serd.SerdConverter
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

@Component
class InternalTopologyBuilder(
    private val meterRegistry: MeterRegistry,
    private val applicationEventPublisher: ApplicationEventPublisher,
) {
    companion object {
        const val CANCEL: Int = 1
        const val UNAVAILABLE: Int = 14
        const val DEADLINE_EXCEEDED = 4
        const val NETWORK_ERROR_THRESHOLD: Double = 0.1
    }

    private val internalSerd: SerdConverter<InternalApiCallEvent> = SerdConverter(InternalApiCallEvent::class.java)
    private val internalMetricsSerd = SerdConverter(InternalApiCallStats::class.java)

    fun build(builder: StreamsBuilder): Topology {
        val internalStream =
            builder.stream(
                ApiEventConstants.INTERNAL_API_CALL_TOPIC,
                Consumed.with(Serdes.String(), internalSerd),
            )

        // 실패율 및 평균 지연시간 게이지를 위한 맵
        val failureRateGaugeMap = ConcurrentHashMap<String, AtomicReference<Double>>()
        val avgLatencyGaugeMap = ConcurrentHashMap<String, AtomicReference<Double>>()

        internalStream
            .groupBy(
                { _, ev -> ev.serviceName },
                Grouped.with(Serdes.String(), internalSerd),
            ).windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(1)))
            .aggregate(
                { InternalApiCallStats() },
                { _, ev, agg ->
                    InternalApiCallStats(
                        total = agg.total + 1,
                        success = agg.success + if (ev.statusCode == 0) 1 else 0,
                        latency = agg.latency + ev.durationMs,
                        networkErrorCount = agg.networkErrorCount + if (ev.statusCode.isNetworkErrorStatusCode()) 1 else 0,
                    )
                },
                Materialized.with(Serdes.String(), internalMetricsSerd),
            ).toStream()
            .foreach { windowKey, stats ->
                val serviceName = windowKey.key()
                val tags = listOf(Tag.of("uri", serviceName))
                val total = stats.total.toDouble()
                val success = stats.success.toDouble()
                val failure = total - success
                val networkErrorRate =
                    if (total > 0) {
                        stats.networkErrorCount / total
                    } else {
                        0.0
                    }
                if (networkErrorRate > NETWORK_ERROR_THRESHOLD) {
                    applicationEventPublisher.publishEvent(CircuitBreakerEvent.open(serviceName))
                } else {
                    applicationEventPublisher.publishEvent(CircuitBreakerEvent.close(serviceName))
                }
                if (total > 0) meterRegistry.counter("internal_api_calls_total", tags).increment(total)
                if (success > 0) meterRegistry.counter("internal_api_calls_success", tags).increment(success)
                if (failure > 0) meterRegistry.counter("internal_api_calls_failure", tags).increment(failure)

                val failureRateRef = failureRateGaugeMap.computeIfAbsent(serviceName) { AtomicReference(0.0) }
                val rate = if (total > 0) failure / total else 0.0
                failureRateRef.set(rate)
                meterRegistry.gauge("internal_api_failure_rate", tags, failureRateRef, AtomicReference<Double>::get)

                val avgLatencyRef = avgLatencyGaugeMap.computeIfAbsent(serviceName) { AtomicReference(0.0) }
                val avgLatency = if (total > 0) stats.latency.toDouble() / total else 0.0
                avgLatencyRef.set(avgLatency)
                meterRegistry.gauge("internal_api_avg_latency_ms", tags, avgLatencyRef, AtomicReference<Double>::get)
            }

        return builder.build()
    }

    private fun Int.isNetworkErrorStatusCode(): Boolean = this == UNAVAILABLE || this == DEADLINE_EXCEEDED || this == CANCEL
}

data class InternalApiCallStats(
    val total: Long = 0L,
    val success: Long = 0L,
    val latency: Long = 0L,
    val networkErrorCount: Long = 0L,
)
