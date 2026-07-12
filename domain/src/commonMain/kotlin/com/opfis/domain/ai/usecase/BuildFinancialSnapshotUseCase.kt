package com.opfis.domain.ai.usecase

import com.opfis.domain.ai.FinancialRepositories
import com.opfis.domain.ai.FinancialSnapshot
import kotlinx.coroutines.flow.first

/** Assembles a one-shot [FinancialSnapshot] for [com.opfis.domain.ai.LocalAiPort] to reason over. */
class BuildFinancialSnapshotUseCase(
    private val repositories: FinancialRepositories,
) {
    suspend operator fun invoke(): FinancialSnapshot =
        FinancialSnapshot(
            accounts = repositories.accounts.observeAll().first(),
            assets = repositories.assets.observeAll().first(),
            liabilities = repositories.liabilities.observeAll().first(),
            transactions = repositories.transactions.observeAll().first(),
            categories = repositories.categories.observeAll().first(),
            budgets = repositories.budgets.observeAll().first(),
            goals = repositories.goals.observeAll().first(),
        )
}
