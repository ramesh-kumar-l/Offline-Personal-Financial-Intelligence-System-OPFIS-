package com.opfis.domain.account

import kotlinx.serialization.Serializable

/**
 * A user-owned financial account (PRD Data Model; ROADMAP Phase 2).
 * [balanceMinorUnits] is denominated in the smallest currency unit (e.g.
 * cents) to avoid floating-point rounding errors, and is maintained by
 * `com.opfis.domain.transaction.FinancialLedgerPort` as transactions are
 * posted - it is never edited directly once non-zero history exists.
 */
@Serializable
data class Account(
    val id: String,
    val name: String,
    val type: AccountType,
    val balanceMinorUnits: Long,
    val isArchived: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
)

@Serializable
enum class AccountType {
    CHECKING,
    SAVINGS,
    CREDIT_CARD,
    CASH,
    INVESTMENT,
    LOAN,
    OTHER,
}
