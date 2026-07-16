package com.opfis.domain.transaction

import kotlinx.serialization.Serializable

/**
 * A single posted financial event against [accountId]. [amountMinorUnits]
 * is always positive; the sign of its effect on account balances is
 * derived from [type] by [TransactionLedgerRules], never stored.
 */
@Serializable
data class Transaction(
    val id: String,
    val accountId: String,
    val categoryId: String? = null,
    val type: TransactionType,
    val amountMinorUnits: Long,
    val transferAccountId: String? = null,
    val description: String = "",
    val occurredAt: Long,
    val createdAt: Long,
    val updatedAt: Long,
) {
    init {
        require(amountMinorUnits > 0) { "amountMinorUnits must be positive" }
        if (type == TransactionType.TRANSFER) {
            requireNotNull(transferAccountId) { "TRANSFER requires a transferAccountId" }
            require(transferAccountId != accountId) { "A transfer cannot target its own source account" }
        } else {
            require(transferAccountId == null) { "transferAccountId is only valid for TRANSFER" }
        }
    }
}

@Serializable
enum class TransactionType {
    INCOME,
    EXPENSE,
    TRANSFER,
}
