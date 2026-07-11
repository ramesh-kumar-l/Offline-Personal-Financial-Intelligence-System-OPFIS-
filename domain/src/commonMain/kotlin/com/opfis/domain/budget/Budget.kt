package com.opfis.domain.budget

/**
 * A spending limit for a category over a recurring [period], starting
 * from [startDate] (epoch millis). Computing spend-to-date against this
 * limit is Dashboard/analytics work (ROADMAP Phase 3+); Phase 2 only
 * owns the budget definition itself.
 */
data class Budget(
    val id: String,
    val categoryId: String,
    val limitMinorUnits: Long,
    val period: BudgetPeriod,
    val startDate: Long,
    val createdAt: Long,
    val updatedAt: Long,
) {
    init {
        require(limitMinorUnits > 0) { "limitMinorUnits must be positive" }
    }
}

enum class BudgetPeriod {
    WEEKLY,
    MONTHLY,
    YEARLY,
}
