package com.opfis.domain.ai

import com.opfis.domain.account.AccountRepository
import com.opfis.domain.asset.AssetRepository
import com.opfis.domain.budget.BudgetRepository
import com.opfis.domain.category.CategoryRepository
import com.opfis.domain.goal.GoalRepository
import com.opfis.domain.liability.LiabilityRepository
import com.opfis.domain.transaction.TransactionRepository

/**
 * Bundles every repository [RuleBasedLocalAiEngine] reads from into one
 * constructor parameter, so [BuildFinancialSnapshotUseCase] stays under
 * detekt's `LongParameterList` threshold.
 */
data class FinancialRepositories(
    val accounts: AccountRepository,
    val assets: AssetRepository,
    val liabilities: LiabilityRepository,
    val transactions: TransactionRepository,
    val categories: CategoryRepository,
    val budgets: BudgetRepository,
    val goals: GoalRepository,
)
