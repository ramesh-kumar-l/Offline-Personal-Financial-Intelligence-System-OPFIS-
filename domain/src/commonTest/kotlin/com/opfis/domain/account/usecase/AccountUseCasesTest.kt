package com.opfis.domain.account.usecase

import com.opfis.domain.account.Account
import com.opfis.domain.account.AccountRepository
import com.opfis.domain.account.AccountType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

private class FakeAccountRepository(
    private val accounts: List<Account> = emptyList(),
) : AccountRepository {
    val upserted = mutableListOf<Account>()
    val deleted = mutableListOf<String>()

    override fun observeAll(): Flow<List<Account>> = flowOf(accounts)

    override fun observeById(id: String): Flow<Account?> = flowOf(accounts.find { it.id == id })

    override suspend fun upsert(account: Account) {
        upserted.add(account)
    }

    override suspend fun delete(id: String) {
        deleted.add(id)
    }
}

class AccountUseCasesTest {
    private val account = Account("acc-1", "Checking", AccountType.CHECKING, 10_000L, createdAt = 0L, updatedAt = 0L)

    @Test
    fun `observe accounts returns the repository stream`() =
        runTest {
            val useCase = ObserveAccountsUseCase(FakeAccountRepository(listOf(account)))
            assertEquals(listOf(account), useCase().first())
        }

    @Test
    fun `upsert account delegates to the repository`() =
        runTest {
            val repository = FakeAccountRepository()
            UpsertAccountUseCase(repository)(account)
            assertEquals(listOf(account), repository.upserted)
        }

    @Test
    fun `delete account delegates to the repository`() =
        runTest {
            val repository = FakeAccountRepository()
            DeleteAccountUseCase(repository)(account.id)
            assertEquals(listOf(account.id), repository.deleted)
        }
}
