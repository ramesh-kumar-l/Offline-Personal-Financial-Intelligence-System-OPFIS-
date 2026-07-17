package com.opfis.domain.liability.usecase

import com.opfis.domain.liability.Liability
import com.opfis.domain.liability.LiabilityRepository
import com.opfis.domain.liability.LiabilityType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

private class FakeLiabilityRepository(
    private val liabilities: List<Liability> = emptyList(),
) : LiabilityRepository {
    val upserted = mutableListOf<Liability>()
    val deleted = mutableListOf<String>()

    override fun observeAll(): Flow<List<Liability>> = flowOf(liabilities)

    override suspend fun upsert(liability: Liability) {
        upserted.add(liability)
    }

    override suspend fun delete(id: String) {
        deleted.add(id)
    }
}

class LiabilityUseCasesTest {
    private val liability = Liability("liab-1", "Car Loan", LiabilityType.LOAN, 20_000L, createdAt = 0L, updatedAt = 0L)

    @Test
    fun `observe liabilities returns the repository stream`() =
        runTest {
            val useCase = ObserveLiabilitiesUseCase(FakeLiabilityRepository(listOf(liability)))
            assertEquals(listOf(liability), useCase().first())
        }

    @Test
    fun `upsert liability delegates to the repository`() =
        runTest {
            val repository = FakeLiabilityRepository()
            UpsertLiabilityUseCase(repository)(liability)
            assertEquals(listOf(liability), repository.upserted)
        }

    @Test
    fun `delete liability delegates to the repository`() =
        runTest {
            val repository = FakeLiabilityRepository()
            DeleteLiabilityUseCase(repository)(liability.id)
            assertEquals(listOf(liability.id), repository.deleted)
        }
}
