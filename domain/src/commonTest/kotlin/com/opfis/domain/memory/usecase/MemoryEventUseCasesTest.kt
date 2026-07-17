package com.opfis.domain.memory.usecase

import com.opfis.domain.entity.EntityRef
import com.opfis.domain.entity.EntityType
import com.opfis.domain.memory.MemoryEvent
import com.opfis.domain.memory.MemoryEventRepository
import com.opfis.domain.memory.MemoryEventType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

private class FakeMemoryEventRepository(
    private val events: List<MemoryEvent> = emptyList(),
) : MemoryEventRepository {
    val upserted = mutableListOf<MemoryEvent>()
    val deleted = mutableListOf<String>()

    override fun observeAll(): Flow<List<MemoryEvent>> = flowOf(events)

    override fun observeBySubject(
        entityType: EntityType,
        entityId: String,
    ): Flow<List<MemoryEvent>> = flowOf(events.filter { it.subject == EntityRef(entityType, entityId) })

    override suspend fun upsert(event: MemoryEvent) {
        upserted.add(event)
    }

    override suspend fun delete(id: String) {
        deleted.add(id)
    }
}

class MemoryEventUseCasesTest {
    private val subject = EntityRef(EntityType.GOAL, "goal-1")
    private val event =
        MemoryEvent(
            "mem-1",
            MemoryEventType.NOTE,
            "Refinanced",
            "",
            subject,
            occurredAt = 0L,
            createdAt = 0L,
            updatedAt = 0L,
        )

    @Test
    fun `observe memory timeline returns the repository stream`() =
        runTest {
            val useCase = ObserveMemoryTimelineUseCase(FakeMemoryEventRepository(listOf(event)))
            assertEquals(listOf(event), useCase().first())
        }

    @Test
    fun `observe memory events for entity filters by subject`() =
        runTest {
            val other = event.copy(id = "mem-2", subject = EntityRef(EntityType.ACCOUNT, "acc-1"))
            val useCase = ObserveMemoryEventsForEntityUseCase(FakeMemoryEventRepository(listOf(event, other)))

            val result = useCase(EntityType.GOAL, "goal-1").first()

            assertEquals(listOf(event), result)
        }

    @Test
    fun `record memory event delegates to the repository`() =
        runTest {
            val repository = FakeMemoryEventRepository()
            RecordMemoryEventUseCase(repository)(event)
            assertEquals(listOf(event), repository.upserted)
        }

    @Test
    fun `delete memory event delegates to the repository`() =
        runTest {
            val repository = FakeMemoryEventRepository()
            DeleteMemoryEventUseCase(repository)(event.id)
            assertEquals(listOf(event.id), repository.deleted)
        }
}
