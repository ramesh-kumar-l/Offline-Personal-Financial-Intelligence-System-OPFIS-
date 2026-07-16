package com.opfis.domain.liability

import kotlinx.serialization.Serializable

/**
 * Money the user owes (PRD: loans, credit cards). Independent of
 * [com.opfis.domain.account.Account] for the same reason as
 * [com.opfis.domain.asset.Asset] - it is tracked by balance snapshot,
 * not by ledger postings, in Phase 2.
 */
@Serializable
data class Liability(
    val id: String,
    val name: String,
    val type: LiabilityType,
    val balanceMinorUnits: Long,
    val interestRateBasisPoints: Int? = null,
    val createdAt: Long,
    val updatedAt: Long,
) {
    init {
        require(interestRateBasisPoints == null || interestRateBasisPoints >= 0) {
            "interestRateBasisPoints must not be negative"
        }
    }
}

@Serializable
enum class LiabilityType {
    LOAN,
    CREDIT_CARD,
    MORTGAGE,
    OTHER,
}
