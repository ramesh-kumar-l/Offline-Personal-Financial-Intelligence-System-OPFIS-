package com.opfis.data.liability

import com.opfis.data.testDatabase
import com.opfis.domain.liability.Liability
import com.opfis.domain.liability.LiabilityType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SqlLiabilityRepositoryTest {
    @Test
    fun `upsert persists an interest rate in basis points`() =
        runTest {
            val repository = SqlLiabilityRepository(testDatabase())
            val liability =
                Liability(
                    id = "loan-1",
                    name = "Home Loan",
                    type = LiabilityType.MORTGAGE,
                    balanceMinorUnits = 3_000_000L,
                    interestRateBasisPoints = 725,
                    createdAt = 0L,
                    updatedAt = 0L,
                )

            repository.upsert(liability)
            val liabilities = repository.observeAll().first()

            assertEquals(725, liabilities.single().interestRateBasisPoints)
        }
}
