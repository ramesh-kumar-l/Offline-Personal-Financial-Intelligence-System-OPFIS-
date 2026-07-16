package com.opfis.data.transaction

import com.opfis.data.db.OpfisDatabase
import com.opfis.data.testDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SqlTransactionRepositoryTest {
    private fun seedTransaction(
        database: OpfisDatabase,
        id: String,
        occurredAt: Long,
    ) {
        database.financialTransactionQueries.insertOrReplace(
            id = id,
            account_id = "acc-1",
            category_id = null,
            type = "EXPENSE",
            amount_minor_units = 100L,
            transfer_account_id = null,
            description = "",
            occurred_at = occurredAt,
            created_at = occurredAt,
            updated_at = occurredAt,
            version = 1L,
        )
    }

    @Test
    fun `observeRecent returns the newest transactions bounded by limit`() =
        runTest {
            val database = testDatabase()
            (1..5).forEach { i -> seedTransaction(database, "tx-$i", occurredAt = i.toLong()) }
            val repository = SqlTransactionRepository(database)

            val result = repository.observeRecent(limit = 2).first()

            assertEquals(listOf("tx-5", "tx-4"), result.map { it.id })
        }
}
