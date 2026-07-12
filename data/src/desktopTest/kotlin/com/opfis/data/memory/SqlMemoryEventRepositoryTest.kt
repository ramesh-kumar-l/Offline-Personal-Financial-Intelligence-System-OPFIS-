package com.opfis.data.memory

import com.opfis.data.testDatabase
import com.opfis.domain.entity.EntityRef
import com.opfis.domain.entity.EntityType
import com.opfis.domain.memory.MemoryEvent
import com.opfis.domain.memory.MemoryEventType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private fun testMemoryEvent(
    id: String = "mem-1",
    subject: EntityRef? = null,
) = MemoryEvent(
    id = id,
    eventType = MemoryEventType.NOTE,
    title = "Paid off the car loan",
    description = "Final installment cleared today.",
    subject = subject,
    occurredAt = 1000L,
    createdAt = 1000L,
    updatedAt = 1000L,
)

class SqlMemoryEventRepositoryTest {
    @Test
    fun `upsert persists a memory event`() =
        runTest {
            val repository = SqlMemoryEventRepository(testDatabase())
            repository.upsert(testMemoryEvent())

            val events = repository.observeAll().first()

            assertEquals("Paid off the car loan", events.single().title)
        }

    @Test
    fun `observeBySubject returns only events for that entity`() =
        runTest {
            val repository = SqlMemoryEventRepository(testDatabase())
            val goalRef = EntityRef(EntityType.GOAL, "goal-1")
            repository.upsert(testMemoryEvent(id = "mem-1", subject = goalRef))
            repository.upsert(testMemoryEvent(id = "mem-2", subject = null))

            val forGoal = repository.observeBySubject(EntityType.GOAL, "goal-1").first()

            assertEquals("mem-1", forGoal.single().id)
        }

    @Test
    fun `delete removes the memory event`() =
        runTest {
            val repository = SqlMemoryEventRepository(testDatabase())
            repository.upsert(testMemoryEvent())

            repository.delete("mem-1")

            assertNull(repository.observeAll().first().firstOrNull())
        }
}
