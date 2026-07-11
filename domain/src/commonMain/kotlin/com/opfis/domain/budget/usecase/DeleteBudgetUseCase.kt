package com.opfis.domain.budget.usecase

import com.opfis.domain.budget.BudgetRepository

class DeleteBudgetUseCase(
    private val repository: BudgetRepository,
) {
    suspend operator fun invoke(id: String) = repository.delete(id)
}
