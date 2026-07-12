package com.opfis.domain.relationship.usecase

import com.opfis.domain.relationship.Relationship
import com.opfis.domain.relationship.RelationshipRepository

class CreateRelationshipUseCase(
    private val repository: RelationshipRepository,
) {
    suspend operator fun invoke(relationship: Relationship) = repository.upsert(relationship)
}
