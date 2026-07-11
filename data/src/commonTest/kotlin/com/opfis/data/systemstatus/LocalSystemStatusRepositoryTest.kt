package com.opfis.data.systemstatus

import com.opfis.domain.systemstatus.TrustIndicatorState
import com.opfis.shared.logging.Logger
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

private class NoOpLogger : Logger {
    override fun debug(tag: String, message: String) = Unit
    override fun info(tag: String, message: String) = Unit
    override fun warn(tag: String, message: String, throwable: Throwable?) = Unit
    override fun error(tag: String, message: String, throwable: Throwable?) = Unit
}

class LocalSystemStatusRepositoryTest {

    @Test
    fun `observe reports no cloud-dependent indicator as ACTIVE without a network call`() = runTest {
        val repository = LocalSystemStatusRepository(NoOpLogger())

        val status = repository.observe().first()

        val encryptedStorage = status.indicators.single { it.id == "encrypted_storage" }
        assertTrue(encryptedStorage.state == TrustIndicatorState.PENDING)

        val offlineMode = status.indicators.single { it.id == "offline_mode" }
        assertTrue(offlineMode.state == TrustIndicatorState.ACTIVE)
    }
}
