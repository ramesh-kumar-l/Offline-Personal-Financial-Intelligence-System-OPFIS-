package com.opfis.domain.goal.usecase

import com.opfis.domain.goal.Goal
import com.opfis.domain.goal.GoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

private class FakeGoalRepository(
    private val goals: List<Goal> = emptyList(),
) : GoalRepository {
    val upserted = mutableListOf<Goal>()
    val deleted = mutableListOf<String>()

    override fun observeAll(): Flow<List<Goal>> = flowOf(goals)

    override suspend fun upsert(goal: Goal) {
        upserted.add(goal)
    }

    override suspend fun delete(id: String) {
        deleted.add(id)
    }
}

class GoalUseCasesTest {
    private val goal = Goal("goal-1", "Emergency Fund", 100_000L, 25_000L, createdAt = 0L, updatedAt = 0L)

    @Test
    fun `observe goals returns the repository stream`() =
        runTest {
            val useCase = ObserveGoalsUseCase(FakeGoalRepository(listOf(goal)))
            assertEquals(listOf(goal), useCase().first())
        }

    @Test
    fun `upsert goal delegates to the repository`() =
        runTest {
            val repository = FakeGoalRepository()
            UpsertGoalUseCase(repository)(goal)
            assertEquals(listOf(goal), repository.upserted)
        }

    @Test
    fun `delete goal delegates to the repository`() =
        runTest {
            val repository = FakeGoalRepository()
            DeleteGoalUseCase(repository)(goal.id)
            assertEquals(listOf(goal.id), repository.deleted)
        }
}
