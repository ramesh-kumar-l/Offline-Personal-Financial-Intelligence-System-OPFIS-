package com.opfis.domain.goal

import kotlin.test.Test
import kotlin.test.assertFailsWith

class GoalTest {
    @Test
    fun `target amount must be positive`() {
        assertFailsWith<IllegalArgumentException> {
            Goal(id = "goal-1", name = "Emergency Fund", targetAmountMinorUnits = 0L, createdAt = 0L, updatedAt = 0L)
        }
    }

    @Test
    fun `current amount must not be negative`() {
        assertFailsWith<IllegalArgumentException> {
            Goal(
                id = "goal-1",
                name = "Emergency Fund",
                targetAmountMinorUnits = 1_000L,
                currentAmountMinorUnits = -1L,
                createdAt = 0L,
                updatedAt = 0L,
            )
        }
    }
}
