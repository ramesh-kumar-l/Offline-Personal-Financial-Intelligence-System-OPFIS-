package com.opfis.data.goal

import com.opfis.data.testDatabase
import com.opfis.domain.goal.Goal
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SqlGoalRepositoryTest {
    @Test
    fun `upsert persists progress toward a target`() =
        runTest {
            val repository = SqlGoalRepository(testDatabase())
            val goal =
                Goal(
                    id = "goal-1",
                    name = "Emergency Fund",
                    targetAmountMinorUnits = 500_000L,
                    currentAmountMinorUnits = 50_000L,
                    createdAt = 0L,
                    updatedAt = 0L,
                )

            repository.upsert(goal)
            repository.upsert(goal.copy(currentAmountMinorUnits = 75_000L))
            val goals = repository.observeAll().first()

            assertEquals(1, goals.size)
            assertEquals(75_000L, goals.single().currentAmountMinorUnits)
        }
}
