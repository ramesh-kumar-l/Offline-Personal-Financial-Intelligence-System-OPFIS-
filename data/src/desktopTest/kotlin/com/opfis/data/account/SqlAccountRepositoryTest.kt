package com.opfis.data.account

import com.opfis.data.testDatabase
import com.opfis.domain.account.Account
import com.opfis.domain.account.AccountType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SqlAccountRepositoryTest {
    private fun account(id: String = "acc-1") =
        Account(
            id = id,
            name = "Checking",
            type = AccountType.CHECKING,
            balanceMinorUnits = 10_000L,
            createdAt = 1000L,
            updatedAt = 1000L,
        )

    @Test
    fun `upsert then observeAll returns the account`() =
        runTest {
            val repository = SqlAccountRepository(testDatabase())

            repository.upsert(account())
            val accounts = repository.observeAll().first()

            assertEquals(1, accounts.size)
            assertEquals("Checking", accounts.single().name)
        }

    @Test
    fun `upsert with the same id updates rather than duplicates`() =
        runTest {
            val repository = SqlAccountRepository(testDatabase())

            repository.upsert(account())
            repository.upsert(account().copy(name = "Renamed"))
            val accounts = repository.observeAll().first()

            assertEquals(1, accounts.size)
            assertEquals("Renamed", accounts.single().name)
        }

    @Test
    fun `delete removes the account`() =
        runTest {
            val repository = SqlAccountRepository(testDatabase())
            repository.upsert(account())

            repository.delete("acc-1")

            assertNull(repository.observeById("acc-1").first())
        }
}
