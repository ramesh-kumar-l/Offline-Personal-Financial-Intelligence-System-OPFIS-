package com.opfis.domain.relationship

import com.opfis.domain.entity.EntityRef
import com.opfis.domain.entity.EntityType
import kotlin.test.Test
import kotlin.test.assertEquals

class KnowledgeGraphBuilderTest {
    @Test
    fun `builds neighbors from both relationship directions`() {
        val root = EntityRef(EntityType.GOAL, "goal-1")
        val account = EntityRef(EntityType.ACCOUNT, "acc-1")
        val document = EntityRef(EntityType.DOCUMENT, "doc-1")
        val relationships =
            listOf(
                Relationship("rel-1", from = root, to = account, RelationshipType.CONTRIBUTES_TO, 0L, 0L),
                Relationship("rel-2", from = document, to = root, RelationshipType.SUPPORTING_DOCUMENT, 0L, 0L),
            )

        val graph = KnowledgeGraphBuilder.build(root, relationships)

        assertEquals(root, graph.root)
        assertEquals(listOf(account, document), graph.neighbors)
        assertEquals(relationships, graph.edges)
    }

    @Test
    fun `an entity with no relationships has an empty graph`() {
        val root = EntityRef(EntityType.ACCOUNT, "acc-1")

        val graph = KnowledgeGraphBuilder.build(root, emptyList())

        assertEquals(emptyList(), graph.neighbors)
        assertEquals(emptyList(), graph.edges)
    }
}
