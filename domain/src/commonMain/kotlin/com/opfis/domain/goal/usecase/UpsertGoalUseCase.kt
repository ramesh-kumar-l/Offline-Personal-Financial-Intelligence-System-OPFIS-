package com.opfis.domain.goal.usecase

import com.opfis.domain.goal.Goal
import com.opfis.domain.goal.GoalRepository

class UpsertGoalUseCase(
    private val repository: GoalRepository,
) {
    suspend operator fun invoke(goal: Goal) = repository.upsert(goal)
}
