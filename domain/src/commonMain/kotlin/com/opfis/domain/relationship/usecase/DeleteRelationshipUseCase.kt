package com.opfis.domain.relationship.usecase

import com.opfis.domain.relationship.RelationshipRepository

class DeleteRelationshipUseCase(
    private val repository: RelationshipRepository,
) {
    suspend operator fun invoke(id: String) = repository.delete(id)
}
