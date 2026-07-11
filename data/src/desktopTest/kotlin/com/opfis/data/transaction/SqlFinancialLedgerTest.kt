package com.opfis.data.transaction

import com.opfis.data.account.SqlAccountRepository
import com.opfis.data.db.OpfisDatabase
import com.opfis.data.testDatabase
import com.opfis.domain.account.Account
import com.opfis.domain.account.AccountType
import com.opfis.domain.transaction.Transaction
import com.opfis.domain.transaction.TransactionType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SqlFinancialLedgerTest {
    private suspend fun seedAccount(
        database: OpfisDatabase,
        id: String,
        balance: Long = 0L,
    ) {
        SqlAccountRepository(database).upsert(
            Account(
                id = id,
                name = id,
                type = AccountType.CHECKING,
                balanceMinorUnits = balance,
                createdAt = 0L,
                updatedAt = 0L,
            ),
        )
    }

    private suspend fun balanceOf(
        database: OpfisDatabase,
        accountId: String,
    ): Long = SqlAccountRepository(database).observeById(accountId).first()!!.balanceMinorUnits

    @Test
    fun `recording income increases the account balance`() =
        runTest {
            val database = testDatabase()
            seedAccount(database, "acc-1", balance = 1_000L)
            val ledger = SqlFinancialLedger(database)

            ledger.recordTransaction(
                Transaction(
                    id = "tx-1",
                    accountId = "acc-1",
                    type = TransactionType.INCOME,
                    amountMinorUnits = 500L,
                    occurredAt = 0L,
                    createdAt = 0L,
                    updatedAt = 0L,
                ),
            )

            assertEquals(1_500L, balanceOf(database, "acc-1"))
        }

    @Test
    fun `recording an expense decreases the account balance`() =
        runTest {
            val database = testDatabase()
            seedAccount(database, "acc-1", balance = 1_000L)
            val ledger = SqlFinancialLedger(database)

            ledger.recordTransaction(
                Transaction(
                    id = "tx-1",
                    accountId = "acc-1",
                    type = TransactionType.EXPENSE,
                    amountMinorUnits = 300L,
                    occurredAt = 0L,
                    createdAt = 0L,
                    updatedAt = 0L,
                ),
            )

            assertEquals(700L, balanceOf(database, "acc-1"))
        }

    @Test
    fun `recording a transfer moves money between two accounts atomically`() =
        runTest {
            val database = testDatabase()
            seedAccount(database, "acc-1", balance = 1_000L)
            seedAccount(database, "acc-2", balance = 200L)
            val ledger = SqlFinancialLedger(database)

            ledger.recordTransaction(
                Transaction(
                    id = "tx-1",
                    accountId = "acc-1",
                    type = TransactionType.TRANSFER,
                    amountMinorUnits = 400L,
                    transferAccountId = "acc-2",
                    occurredAt = 0L,
                    createdAt = 0L,
                    updatedAt = 0L,
                ),
            )

            assertEquals(600L, balanceOf(database, "acc-1"))
            assertEquals(600L, balanceOf(database, "acc-2"))
        }

    @Test
    fun `deleting a transaction reverses its balance effect`() =
        runTest {
            val database = testDatabase()
            seedAccount(database, "acc-1", balance = 1_000L)
            seedAccount(database, "acc-2", balance = 200L)
            val ledger = SqlFinancialLedger(database)
            ledger.recordTransaction(
                Transaction(
                    id = "tx-1",
                    accountId = "acc-1",
                    type = TransactionType.TRANSFER,
                    amountMinorUnits = 400L,
                    transferAccountId = "acc-2",
                    occurredAt = 0L,
                    createdAt = 0L,
                    updatedAt = 0L,
                ),
            )

            ledger.deleteTransaction("tx-1")

            assertEquals(1_000L, balanceOf(database, "acc-1"))
            assertEquals(200L, balanceOf(database, "acc-2"))
            assertEquals(0, SqlTransactionRepository(database).observeAll().first().size)
        }

    @Test
    fun `deleting an unknown transaction id is a no-op`() =
        runTest {
            val database = testDatabase()
            seedAccount(database, "acc-1", balance = 1_000L)
            val ledger = SqlFinancialLedger(database)

            ledger.deleteTransaction("does-not-exist")

            assertEquals(1_000L, balanceOf(database, "acc-1"))
        }
}
