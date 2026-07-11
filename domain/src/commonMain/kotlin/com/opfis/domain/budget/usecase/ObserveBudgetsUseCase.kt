package com.opfis.domain.budget.usecase

import com.opfis.domain.budget.Budget
import com.opfis.domain.budget.BudgetRepository
import kotlinx.coroutines.flow.Flow

class ObserveBudgetsUseCase(
    private val repository: BudgetRepository,
) {
    operator fun invoke(): Flow<List<Budget>> = repository.observeAll()
}
