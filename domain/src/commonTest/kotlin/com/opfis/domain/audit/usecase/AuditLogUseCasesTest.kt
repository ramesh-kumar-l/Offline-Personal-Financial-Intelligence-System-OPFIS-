package com.opfis.domain.audit.usecase

import com.opfis.domain.audit.AuditEventType
import com.opfis.domain.audit.AuditLogEntry
import com.opfis.domain.audit.AuditLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

private class FakeAuditLogRepository(
    private val entries: List<AuditLogEntry> = emptyList(),
) : AuditLogRepository {
    val recorded = mutableListOf<AuditLogEntry>()

    override fun observeAll(): Flow<List<AuditLogEntry>> = flowOf(entries)

    override suspend fun record(entry: AuditLogEntry) {
        recorded.add(entry)
    }
}

class AuditLogUseCasesTest {
    @Test
    fun `record audit event delegates to the repository`() =
        runTest {
            val repository = FakeAuditLogRepository()
            val entry = AuditLogEntry("id-1", AuditEventType.APP_UNLOCKED, "Unlocked via biometric authentication", 0L)

            RecordAuditEventUseCase(repository)(entry)

            assertEquals(listOf(entry), repository.recorded)
        }

    @Test
    fun `observe audit log returns the repository stream`() =
        runTest {
            val entry = AuditLogEntry("id-1", AuditEventType.BACKUP_EXPORTED, "Backup exported", 0L)
            val useCase = ObserveAuditLogUseCase(FakeAuditLogRepository(listOf(entry)))

            assertEquals(listOf(entry), useCase().first())
        }
}
