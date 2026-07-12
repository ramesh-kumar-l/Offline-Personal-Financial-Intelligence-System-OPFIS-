package com.opfis.domain.security

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AutoLockPolicyTest {
    @Test
    fun `does not lock before the timeout elapses`() {
        val locked =
            AutoLockPolicy.shouldLock(
                lastInteractionAtEpochMillis = 0L,
                nowEpochMillis = 1_000L,
                timeoutMillis = 5_000L,
            )
        assertFalse(locked)
    }

    @Test
    fun `locks once the timeout has elapsed`() {
        val locked =
            AutoLockPolicy.shouldLock(
                lastInteractionAtEpochMillis = 0L,
                nowEpochMillis = 5_000L,
                timeoutMillis = 5_000L,
            )
        assertTrue(locked)
    }

    @Test
    fun `locks well past the timeout`() {
        val locked =
            AutoLockPolicy.shouldLock(
                lastInteractionAtEpochMillis = 0L,
                nowEpochMillis = 999_999L,
                timeoutMillis = 5_000L,
            )
        assertTrue(locked)
    }
}
