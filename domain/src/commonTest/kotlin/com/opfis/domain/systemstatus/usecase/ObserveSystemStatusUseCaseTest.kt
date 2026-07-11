package com.opfis.domain.systemstatus.usecase

import com.opfis.domain.systemstatus.SystemStatus
import com.opfis.domain.systemstatus.SystemStatusRepository
import com.opfis.domain.systemstatus.TrustIndicator
import com.opfis.domain.systemstatus.TrustIndicatorState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

private class FakeSystemStatusRepository(
    private val status: SystemStatus,
) : SystemStatusRepository {
    override fun observe(): Flow<SystemStatus> = flowOf(status)
}

class ObserveSystemStatusUseCaseTest {
    @Test
    fun `invoke emits the status produced by the repository`() =
        runTest {
            val expected =
                SystemStatus(
                    indicators =
                        listOf(
                            TrustIndicator(
                                id = "offline_mode",
                                label = "Offline Mode",
                                state = TrustIndicatorState.ACTIVE,
                            ),
                        ),
                )
            val useCase = ObserveSystemStatusUseCase(FakeSystemStatusRepository(expected))

            val actual = useCase().first()

            assertEquals(expected, actual)
        }
}
