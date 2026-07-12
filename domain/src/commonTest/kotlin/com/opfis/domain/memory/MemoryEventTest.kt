package com.opfis.domain.memory

import kotlin.test.Test
import kotlin.test.assertFailsWith

class MemoryEventTest {
    @Test
    fun `a memory event cannot have a blank title`() {
        assertFailsWith<IllegalArgumentException> {
            MemoryEvent(
                id = "mem-1",
                eventType = MemoryEventType.NOTE,
                title = "   ",
                description = "",
                subject = null,
                occurredAt = 0L,
                createdAt = 0L,
                updatedAt = 0L,
            )
        }
    }
}
