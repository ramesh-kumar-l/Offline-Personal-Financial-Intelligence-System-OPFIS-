package com.opfis.domain.systemstatus

/**
 * A single trust indicator shown on the dashboard (SystemPrompt Part 3,
 * "Trust Indicators"). Each indicator reassures the user about a
 * specific privacy or reliability guarantee; it never alarms.
 */
data class TrustIndicator(
    val id: String,
    val label: String,
    val state: TrustIndicatorState,
)

enum class TrustIndicatorState {
    ACTIVE,
    PENDING,
}

/**
 * The set of trust indicators describing the current runtime, e.g.
 * "Offline Mode", "Local Storage", "Encryption". Populated by
 * [SystemStatusRepository] and consumed by the Presentation layer
 * through [com.opfis.domain.systemstatus.usecase.ObserveSystemStatusUseCase].
 */
data class SystemStatus(
    val indicators: List<TrustIndicator>,
)
