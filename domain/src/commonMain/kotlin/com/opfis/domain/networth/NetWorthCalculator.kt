package com.opfis.domain.networth

import com.opfis.domain.account.Account
import com.opfis.domain.asset.Asset
import com.opfis.domain.liability.Liability

/**
 * Pure domain policy (no SQL/framework dependency, unit-testable without
 * a database - SystemPrompt Part 2) that turns the current account,
 * asset, and liability snapshots into a [NetWorthSummary]. Archived
 * accounts are excluded: they no longer represent money the user can
 * act on.
 */
object NetWorthCalculator {
    fun calculate(
        accounts: List<Account>,
        assets: List<Asset>,
        liabilities: List<Liability>,
    ): NetWorthSummary =
        NetWorthSummary(
            accountBalanceMinorUnits = accounts.filterNot { it.isArchived }.sumOf { it.balanceMinorUnits },
            assetValueMinorUnits = assets.sumOf { it.valueMinorUnits },
            liabilityBalanceMinorUnits = liabilities.sumOf { it.balanceMinorUnits },
        )
}
