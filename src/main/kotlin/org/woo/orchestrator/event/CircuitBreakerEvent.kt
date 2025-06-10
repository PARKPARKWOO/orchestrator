package org.woo.orchestrator.event

data class CircuitBreakerEvent(
    val serviceName: String,
    val isOpen: Boolean,
) {
    companion object {
        fun open(serviceName: String): CircuitBreakerEvent = CircuitBreakerEvent(serviceName, true)

        fun close(serviceName: String): CircuitBreakerEvent = CircuitBreakerEvent(serviceName, false)
    }
}
