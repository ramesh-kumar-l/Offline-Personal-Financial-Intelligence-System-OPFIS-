package com.opfis.domain.asset

import kotlinx.serialization.Serializable

/**
 * A non-account item of value the user owns (PRD: stocks, mutual funds,
 * gold, crypto, real estate, EPF/PPF/NPS). Kept independent of
 * [com.opfis.domain.account.Account] - an asset is tracked by valuation,
 * not by transaction postings.
 */
@Serializable
data class Asset(
    val id: String,
    val name: String,
    val type: AssetType,
    val valueMinorUnits: Long,
    val createdAt: Long,
    val updatedAt: Long,
)

@Serializable
enum class AssetType {
    REAL_ESTATE,
    VEHICLE,
    EQUITY,
    MUTUAL_FUND,
    RETIREMENT_FUND,
    GOLD,
    CRYPTO,
    OTHER,
}
