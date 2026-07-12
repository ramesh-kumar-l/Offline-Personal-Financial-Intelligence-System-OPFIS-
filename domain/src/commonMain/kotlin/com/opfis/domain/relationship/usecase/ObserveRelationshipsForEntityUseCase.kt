package com.opfis.domain.relationship.usecase

import com.opfis.domain.entity.EntityType
import com.opfis.domain.relationship.Relationship
import com.opfis.domain.relationship.RelationshipRepository
import kotlinx.coroutines.flow.Flow

class ObserveRelationshipsForEntityUseCase(
    private val repository: RelationshipRepository,
) {
    operator fun invoke(
        entityType: EntityType,
        entityId: String,
    ): Flow<List<Relationship>> = repository.observeInvolving(entityType, entityId)
}
