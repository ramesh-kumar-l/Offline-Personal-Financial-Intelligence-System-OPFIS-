package com.opfis.domain.security

/**
 * Pure idle-timeout policy for ROADMAP Phase 8's "Auto-lock" (same
 * framework-free policy-object pattern as `transaction/TransactionLedgerRules`
 * and `networth/NetWorthCalculator`). The Presentation layer resets
 * [lastInteractionAtEpochMillis] on every user interaction and re-checks
 * this on a timer; it owns the actual lock/unlock state.
 */
object AutoLockPolicy {
    const val DEFAULT_TIMEOUT_MILLIS: Long = 5 * 60 * 1000L

    fun shouldLock(
        lastInteractionAtEpochMillis: Long,
        nowEpochMillis: Long,
        timeoutMillis: Long = DEFAULT_TIMEOUT_MILLIS,
    ): Boolean = nowEpochMillis - lastInteractionAtEpochMillis >= timeoutMillis
}
