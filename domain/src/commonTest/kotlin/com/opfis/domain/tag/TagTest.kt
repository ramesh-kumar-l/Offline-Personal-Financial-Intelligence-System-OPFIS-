package com.opfis.domain.tag

import kotlin.test.Test
import kotlin.test.assertFailsWith

class TagTest {
    @Test
    fun `a tag cannot have a blank name`() {
        assertFailsWith<IllegalArgumentException> {
            Tag(id = "tag-1", name = "   ", createdAt = 0L, updatedAt = 0L)
        }
    }
}
