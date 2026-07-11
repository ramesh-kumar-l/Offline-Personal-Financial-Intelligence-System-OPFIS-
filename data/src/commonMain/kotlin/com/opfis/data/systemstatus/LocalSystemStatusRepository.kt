package com.opfis.data.systemstatus

import com.opfis.domain.systemstatus.SystemStatus
import com.opfis.domain.systemstatus.SystemStatusRepository
import com.opfis.domain.systemstatus.TrustIndicator
import com.opfis.domain.systemstatus.TrustIndicatorState
import com.opfis.shared.logging.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Phase 0 implementation of [SystemStatusRepository]. Reports the trust
 * indicators that are true of the codebase today - no network calls,
 * no cloud dependency - and marks the ones that depend on later phases
 * (encrypted storage in Phase 1, local AI in Phase 7) as PENDING rather
 * than fabricating them.
 *
 * This will be replaced once Phase 1 introduces real persistence to
 * observe: it will report the actual SQLCipher encryption state, last
 * backup time, and AI runtime availability instead of static values.
 */
class LocalSystemStatusRepository(
    private val logger: Logger,
) : SystemStatusRepository {

    override fun observe(): Flow<SystemStatus> = flow {
        logger.debug(TAG, "Emitting current system status")
        emit(
            SystemStatus(
                indicators = listOf(
                    TrustIndicator(
                        id = "offline_mode",
                        label = "Offline Mode",
                        state = TrustIndicatorState.ACTIVE,
                    ),
                    TrustIndicator(
                        id = "no_cloud_connected",
                        label = "No Cloud Connected",
                        state = TrustIndicatorState.ACTIVE,
                    ),
                    TrustIndicator(
                        id = "encrypted_storage",
                        label = "Encrypted Storage",
                        state = TrustIndicatorState.PENDING,
                    ),
                    TrustIndicator(
                        id = "local_ai",
                        label = "Local AI",
                        state = TrustIndicatorState.PENDING,
                    ),
                ),
            ),
        )
    }

    private companion object {
        const val TAG = "LocalSystemStatusRepository"
    }
}
