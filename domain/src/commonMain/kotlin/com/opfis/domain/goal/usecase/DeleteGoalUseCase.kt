package com.opfis.domain.goal.usecase

import com.opfis.domain.goal.GoalRepository

class DeleteGoalUseCase(
    private val repository: GoalRepository,
) {
    suspend operator fun invoke(id: String) = repository.delete(id)
}
