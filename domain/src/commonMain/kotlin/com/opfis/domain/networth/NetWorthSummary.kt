package com.opfis.domain.networth

/**
 * A point-in-time snapshot of financial health (SystemPrompt Part 3,
 * "Home Dashboard": Net Worth). Components are kept separate so the
 * Presentation layer can chart asset composition (accounts vs. other
 * assets) without re-deriving it from raw entity lists.
 */
data class NetWorthSummary(
    val accountBalanceMinorUnits: Long,
    val assetValueMinorUnits: Long,
    val liabilityBalanceMinorUnits: Long,
) {
    val totalAssetsMinorUnits: Long get() = accountBalanceMinorUnits + assetValueMinorUnits

    val netWorthMinorUnits: Long get() = totalAssetsMinorUnits - liabilityBalanceMinorUnits
}
