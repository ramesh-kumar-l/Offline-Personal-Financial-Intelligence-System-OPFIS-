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

private class FakeRelationshipRepository(
    private val relationships: List<Relationship>,
) : RelationshipRepository {
    override fun observeInvolving(
        entityType: EntityType,
        entityId: String,
    ): Flow<List<Relationship>> = flowOf(relationships)

    override suspend fun upsert(relationship: Relationship) = error("not used in this test")

    override suspend fun delete(id: String) = error("not used in this test")
}

class ObserveKnowledgeGraphUseCaseTest {
    @Test
    fun `builds a graph rooted at the requested entity`() =
        runTest {
            val root = EntityRef(EntityType.GOAL, "goal-1")
            val account = EntityRef(EntityType.ACCOUNT, "acc-1")
            val relationship = Relationship("rel-1", root, account, RelationshipType.CONTRIBUTES_TO, 0L, 0L)
            val useCase = ObserveKnowledgeGraphUseCase(FakeRelationshipRepository(listOf(relationship)))

            val graph = useCase(EntityType.GOAL, "goal-1").first()

            assertEquals(root, graph.root)
            assertEquals(listOf(account), graph.neighbors)
        }
}
