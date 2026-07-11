package com.opfis.domain.category

import kotlin.test.Test
import kotlin.test.assertFailsWith

class CategoryTest {
    @Test
    fun `a category cannot be its own parent`() {
        assertFailsWith<IllegalArgumentException> {
            Category(
                id = "cat-1",
                name = "Food",
                type = CategoryType.EXPENSE,
                parentId = "cat-1",
                createdAt = 0L,
                updatedAt = 0L,
            )
        }
    }
}
