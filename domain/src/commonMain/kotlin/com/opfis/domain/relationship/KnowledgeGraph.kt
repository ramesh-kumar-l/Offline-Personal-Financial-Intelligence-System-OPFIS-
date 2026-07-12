package com.opfis.domain.relationship

import com.opfis.domain.entity.EntityRef

/**
 * A minimal, 1-hop read-only view of a root entity's declared
 * [Relationship]s (ROADMAP Phase 6, "Knowledge graph abstractions").
 * Deliberately not a full transitive graph traversal across every
 * entity's existing foreign keys - see [Relationship]'s scope note;
 * this graph only surfaces relationships a user has explicitly
 * declared.
 */
data class KnowledgeGraph(
    val root: EntityRef,
    val neighbors: List<EntityRef>,
    val edges: List<Relationship>,
)

object KnowledgeGraphBuilder {
    fun build(
        root: EntityRef,
        relationships: List<Relationship>,
    ): KnowledgeGraph {
        val neighbors = relationships.map { if (it.from == root) it.to else it.from }
        return KnowledgeGraph(root = root, neighbors = neighbors, edges = relationships)
    }
}
