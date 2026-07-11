package com.opfis.data.systemstatus

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.opfis.data.db.OpfisDatabase
import com.opfis.domain.systemstatus.TrustIndicatorState
import com.opfis.shared.logging.Logger
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import java.util.Properties
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class NoOpLogger : Logger {
    override fun debug(
        tag: String,
        message: String,
    ) = Unit

    override fun info(
        tag: String,
        message: String,
    ) = Unit

    override fun warn(
        tag: String,
        message: String,
        throwable: Throwable?,
    ) = Unit

    override fun error(
        tag: String,
        message: String,
        throwable: Throwable?,
    ) = Unit
}

class PersistentSystemStatusRepositoryTest {
    private fun newDatabase(): OpfisDatabase {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY, Properties(), OpfisDatabase.Schema)
        return OpfisDatabase(driver)
    }

    @Test
    fun `seeds default indicators on first observe`() =
        runTest {
            val repository = PersistentSystemStatusRepository(newDatabase(), NoOpLogger())

            val status = repository.observe().first()

            assertEquals(4, status.indicators.size)
            val encryptedStorage = status.indicators.single { it.id == "encrypted_storage" }
            assertEquals(TrustIndicatorState.ACTIVE, encryptedStorage.state)
            val localAi = status.indicators.single { it.id == "local_ai" }
            assertEquals(TrustIndicatorState.PENDING, localAi.state)
        }

    @Test
    fun `does not duplicate rows across repository instances over the same database`() =
        runTest {
            val database = newDatabase()
            PersistentSystemStatusRepository(database, NoOpLogger())

            val secondRepository = PersistentSystemStatusRepository(database, NoOpLogger())
            val status = secondRepository.observe().first()

            assertEquals(4, status.indicators.size)
        }

    @Test
    fun `state updates are reflected by subsequent reads`() =
        runTest {
            val database = newDatabase()
            PersistentSystemStatusRepository(database, NoOpLogger())

            database.systemStatusQueries.updateState(
                state = TrustIndicatorState.PENDING.name,
                updated_at = 123L,
                id = "offline_mode",
            )
            val row = database.systemStatusQueries.selectById("offline_mode").executeAsOne()

            assertEquals(TrustIndicatorState.PENDING.name, row.state)
            assertTrue(row.version > 1)
        }
}
