package com.opfis.domain.ai

import com.opfis.domain.account.Account
import com.opfis.domain.asset.Asset
import com.opfis.domain.budget.Budget
import com.opfis.domain.category.Category
import com.opfis.domain.goal.Goal
import com.opfis.domain.liability.Liability
import com.opfis.domain.transaction.Transaction

/** A one-shot, in-memory snapshot of the user's financial data, built by [BuildFinancialSnapshotUseCase]. */
data class FinancialSnapshot(
    val accounts: List<Account>,
    val assets: List<Asset>,
    val liabilities: List<Liability>,
    val transactions: List<Transaction>,
    val categories: List<Category>,
    val budgets: List<Budget>,
    val goals: List<Goal>,
)
