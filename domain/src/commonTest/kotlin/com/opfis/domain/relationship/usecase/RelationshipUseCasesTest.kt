package com.opfis.domain.relationship.usecase

import com.opfis.domain.entity.EntityRef
import com.opfis.domain.entity.EntityType
import com.opfis.domain.relationship.Relationship
import com.opfis.domain.relationship.RelationshipRepository
import com.opfis.domain.relationship.RelationshipType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

private class FakeRelationshipCrudRepository(
    private val relationships: List<Relationship> = emptyList(),
) : RelationshipRepository {
    val upserted = mutableListOf<Relationship>()
    val deleted = mutableListOf<String>()

    override fun observeAll(): Flow<List<Relationship>> = flowOf(relationships)

    override fun observeInvolving(
        entityType: EntityType,
        entityId: String,
    ): Flow<List<Relationship>> {
        val ref = EntityRef(entityType, entityId)
        return flowOf(relationships.filter { it.from == ref || it.to == ref })
    }

    override suspend fun upsert(relationship: Relationship) {
        upserted.add(relationship)
    }

    override suspend fun delete(id: String) {
        deleted.add(id)
    }
}

class RelationshipUseCasesTest {
    private val root = EntityRef(EntityType.GOAL, "goal-1")
    private val account = EntityRef(EntityType.ACCOUNT, "acc-1")
    private val relationship = Relationship("rel-1", root, account, RelationshipType.CONTRIBUTES_TO, 0L, 0L)

    @Test
    fun `create relationship delegates to the repository`() =
        runTest {
            val repository = FakeRelationshipCrudRepository()
            CreateRelationshipUseCase(repository)(relationship)
            assertEquals(listOf(relationship), repository.upserted)
        }

    @Test
    fun `delete relationship delegates to the repository`() =
        runTest {
            val repository = FakeRelationshipCrudRepository()
            DeleteRelationshipUseCase(repository)(relationship.id)
            assertEquals(listOf(relationship.id), repository.deleted)
        }

    @Test
    fun `observe relationships for entity filters by either endpoint`() =
        runTest {
            val useCase = ObserveRelationshipsForEntityUseCase(FakeRelationshipCrudRepository(listOf(relationship)))
            assertEquals(listOf(relationship), useCase(EntityType.ACCOUNT, "acc-1").first())
        }
}
