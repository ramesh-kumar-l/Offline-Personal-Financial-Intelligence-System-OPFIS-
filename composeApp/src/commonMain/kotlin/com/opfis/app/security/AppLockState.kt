package com.opfis.app.security

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.opfis.domain.security.AutoLockPolicy
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Presentation-level lock state for ROADMAP Phase 8's "Auto-lock". The
 * app starts locked; navigating between bottom-nav destinations counts
 * as interaction (a lighter-weight signal than raw touch tracking,
 * deliberately - see `09-security-model.md`) and re-arms the idle
 * timer checked by [checkIdleTimeout] against [AutoLockPolicy].
 */
@OptIn(ExperimentalTime::class)
@Stable
class AppLockState {
    var isLocked by mutableStateOf(true)
        private set

    private var lastInteractionAtEpochMillis: Long = nowMillis()

    fun recordInteraction() {
        lastInteractionAtEpochMillis = nowMillis()
    }

    fun unlock() {
        isLocked = false
        recordInteraction()
    }

    fun checkIdleTimeout() {
        if (!isLocked && AutoLockPolicy.shouldLock(lastInteractionAtEpochMillis, nowMillis())) {
            isLocked = true
        }
    }

    private fun nowMillis(): Long = Clock.System.now().toEpochMilliseconds()
}
