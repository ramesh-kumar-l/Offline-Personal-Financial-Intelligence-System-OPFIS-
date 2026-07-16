package com.opfis.domain.goal

import kotlinx.serialization.Serializable

/**
 * A savings target the user is tracking toward (PRD: financial goals).
 * [currentAmountMinorUnits] is a user/UX-facing progress snapshot, not a
 * ledger-derived balance - it is deliberately editable via upsert,
 * unlike [com.opfis.domain.account.Account.balanceMinorUnits].
 */
@Serializable
data class Goal(
    val id: String,
    val name: String,
    val targetAmountMinorUnits: Long,
    val currentAmountMinorUnits: Long = 0,
    val targetDate: Long? = null,
    val createdAt: Long,
    val updatedAt: Long,
) {
    init {
        require(targetAmountMinorUnits > 0) { "targetAmountMinorUnits must be positive" }
        require(currentAmountMinorUnits >= 0) { "currentAmountMinorUnits must not be negative" }
    }
}
