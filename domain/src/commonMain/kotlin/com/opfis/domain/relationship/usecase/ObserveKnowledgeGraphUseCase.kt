package com.opfis.domain.relationship.usecase

import com.opfis.domain.entity.EntityRef
import com.opfis.domain.entity.EntityType
import com.opfis.domain.relationship.KnowledgeGraph
import com.opfis.domain.relationship.KnowledgeGraphBuilder
import com.opfis.domain.relationship.RelationshipRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveKnowledgeGraphUseCase(
    private val repository: RelationshipRepository,
) {
    operator fun invoke(
        entityType: EntityType,
        entityId: String,
    ): Flow<KnowledgeGraph> {
        val root = EntityRef(entityType, entityId)
        return repository.observeInvolving(entityType, entityId).map { relationships ->
            KnowledgeGraphBuilder.build(root, relationships)
        }
    }
}
