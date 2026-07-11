package com.opfis.domain.networth

import com.opfis.domain.account.Account
import com.opfis.domain.account.AccountType
import com.opfis.domain.asset.Asset
import com.opfis.domain.asset.AssetType
import com.opfis.domain.liability.Liability
import com.opfis.domain.liability.LiabilityType
import kotlin.test.Test
import kotlin.test.assertEquals

class NetWorthCalculatorTest {
    private fun account(
        balance: Long,
        isArchived: Boolean = false,
    ) = Account(
        id = "acc-1",
        name = "Checking",
        type = AccountType.CHECKING,
        balanceMinorUnits = balance,
        isArchived = isArchived,
        createdAt = 0L,
        updatedAt = 0L,
    )

    private fun asset(value: Long) =
        Asset(
            id = "asset-1",
            name = "Gold",
            type = AssetType.GOLD,
            valueMinorUnits = value,
            createdAt = 0L,
            updatedAt = 0L,
        )

    private fun liability(balance: Long) =
        Liability(
            id = "liability-1",
            name = "Car Loan",
            type = LiabilityType.LOAN,
            balanceMinorUnits = balance,
            createdAt = 0L,
            updatedAt = 0L,
        )

    @Test
    fun `empty inputs produce a zero net worth`() {
        val summary = NetWorthCalculator.calculate(emptyList(), emptyList(), emptyList())

        assertEquals(NetWorthSummary(0L, 0L, 0L), summary)
    }

    @Test
    fun `net worth sums accounts and assets then subtracts liabilities`() {
        val summary =
            NetWorthCalculator.calculate(
                accounts = listOf(account(balance = 10_000L)),
                assets = listOf(asset(value = 5_000L)),
                liabilities = listOf(liability(balance = 3_000L)),
            )

        assertEquals(10_000L, summary.accountBalanceMinorUnits)
        assertEquals(5_000L, summary.assetValueMinorUnits)
        assertEquals(3_000L, summary.liabilityBalanceMinorUnits)
        assertEquals(15_000L, summary.totalAssetsMinorUnits)
        assertEquals(12_000L, summary.netWorthMinorUnits)
    }

    @Test
    fun `archived accounts are excluded from net worth`() {
        val summary =
            NetWorthCalculator.calculate(
                accounts = listOf(account(balance = 10_000L), account(balance = 999_999L, isArchived = true)),
                assets = emptyList(),
                liabilities = emptyList(),
            )

        assertEquals(10_000L, summary.accountBalanceMinorUnits)
    }
}
