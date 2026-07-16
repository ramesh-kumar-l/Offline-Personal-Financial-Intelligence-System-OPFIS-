package com.opfis.domain.relationship

import com.opfis.domain.entity.EntityType
import kotlinx.coroutines.flow.Flow

interface RelationshipRepository {
    /** Every relationship in the store (ROADMAP Phase 9 full-dataset export). */
    fun observeAll(): Flow<List<Relationship>>

    /** Every relationship where the given entity is either endpoint. */
    fun observeInvolving(
        entityType: EntityType,
        entityId: String,
    ): Flow<List<Relationship>>

    suspend fun upsert(relationship: Relationship)

    suspend fun delete(id: String)
}
