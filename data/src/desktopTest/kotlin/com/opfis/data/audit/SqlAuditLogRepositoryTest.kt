package com.opfis.data.audit

import com.opfis.data.testDatabase
import com.opfis.domain.audit.AuditEventType
import com.opfis.domain.audit.AuditLogEntry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SqlAuditLogRepositoryTest {
    @Test
    fun `record persists an audit entry`() =
        runTest {
            val repository = SqlAuditLogRepository(testDatabase())
            val entry =
                AuditLogEntry("audit-1", AuditEventType.APP_UNLOCKED, "Unlocked via biometric", occurredAt = 1_000L)

            repository.record(entry)

            assertEquals(listOf(entry), repository.observeAll().first())
        }

    @Test
    fun `observe all returns entries newest first`() =
        runTest {
            val repository = SqlAuditLogRepository(testDatabase())
            val older = AuditLogEntry("audit-1", AuditEventType.APP_UNLOCKED, "Unlocked", occurredAt = 1_000L)
            val newer = AuditLogEntry("audit-2", AuditEventType.BACKUP_EXPORTED, "Backup exported", occurredAt = 2_000L)

            repository.record(older)
            repository.record(newer)

            assertEquals(listOf(newer, older), repository.observeAll().first())
        }
}
