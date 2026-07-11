package com.opfis.domain.budget.usecase

import com.opfis.domain.budget.Budget
import com.opfis.domain.budget.BudgetRepository

class UpsertBudgetUseCase(
    private val repository: BudgetRepository,
) {
    suspend operator fun invoke(budget: Budget) = repository.upsert(budget)
}
