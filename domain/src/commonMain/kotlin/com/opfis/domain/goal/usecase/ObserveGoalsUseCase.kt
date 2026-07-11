package com.opfis.domain.goal.usecase

import com.opfis.domain.goal.Goal
import com.opfis.domain.goal.GoalRepository
import kotlinx.coroutines.flow.Flow

class ObserveGoalsUseCase(
    private val repository: GoalRepository,
) {
    operator fun invoke(): Flow<List<Goal>> = repository.observeAll()
}
