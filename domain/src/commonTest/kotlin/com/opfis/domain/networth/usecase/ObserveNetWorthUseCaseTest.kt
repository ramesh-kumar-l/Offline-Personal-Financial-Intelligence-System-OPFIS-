package com.opfis.domain.networth.usecase

import com.opfis.domain.account.Account
import com.opfis.domain.account.AccountRepository
import com.opfis.domain.account.AccountType
import com.opfis.domain.asset.Asset
import com.opfis.domain.asset.AssetRepository
import com.opfis.domain.asset.AssetType
import com.opfis.domain.liability.Liability
import com.opfis.domain.liability.LiabilityRepository
import com.opfis.domain.liability.LiabilityType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

private class FakeAccountRepository(
    private val accounts: List<Account>,
) : AccountRepository {
    override fun observeAll(): Flow<List<Account>> = flowOf(accounts)

    override fun observeById(id: String): Flow<Account?> = flowOf(accounts.find { it.id == id })

    override suspend fun upsert(account: Account) = error("not used in this test")

    override suspend fun delete(id: String) = error("not used in this test")
}

private class FakeAssetRepository(
    private val assets: List<Asset>,
) : AssetRepository {
    override fun observeAll(): Flow<List<Asset>> = flowOf(assets)

    override suspend fun upsert(asset: Asset) = error("not used in this test")

    override suspend fun delete(id: String) = error("not used in this test")
}

private class FakeLiabilityRepository(
    private val liabilities: List<Liability>,
) : LiabilityRepository {
    override fun observeAll(): Flow<List<Liability>> = flowOf(liabilities)

    override suspend fun upsert(liability: Liability) = error("not used in this test")

    override suspend fun delete(id: String) = error("not used in this test")
}

class ObserveNetWorthUseCaseTest {
    @Test
    fun `invoke combines all three repositories into one summary`() =
        runTest {
            val account =
                Account(
                    id = "acc-1",
                    name = "Checking",
                    type = AccountType.CHECKING,
                    balanceMinorUnits = 10_000L,
                    createdAt = 0L,
                    updatedAt = 0L,
                )
            val asset =
                Asset(
                    id = "asset-1",
                    name = "Gold",
                    type = AssetType.GOLD,
                    valueMinorUnits = 5_000L,
                    createdAt = 0L,
                    updatedAt = 0L,
                )
            val liability =
                Liability(
                    id = "liability-1",
                    name = "Loan",
                    type = LiabilityType.LOAN,
                    balanceMinorUnits = 3_000L,
                    createdAt = 0L,
                    updatedAt = 0L,
                )
            val useCase =
                ObserveNetWorthUseCase(
                    FakeAccountRepository(listOf(account)),
                    FakeAssetRepository(listOf(asset)),
                    FakeLiabilityRepository(listOf(liability)),
                )

            val summary = useCase().first()

            assertEquals(12_000L, summary.netWorthMinorUnits)
        }
}
