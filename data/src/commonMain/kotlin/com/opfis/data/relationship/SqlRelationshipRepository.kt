package com.opfis.data.relationship

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.opfis.data.db.OpfisDatabase
import com.opfis.domain.entity.EntityType
import com.opfis.domain.relationship.Relationship
import com.opfis.domain.relationship.RelationshipRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SqlRelationshipRepository(
    private val database: OpfisDatabase,
) : RelationshipRepository {
    override fun observeInvolving(
        entityType: EntityType,
        entityId: String,
    ): Flow<List<Relationship>> =
        database.relationshipQueries
            .selectInvolving(entityType.name, entityId)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> rows.map(::toDomainRelationship) }

    override suspend fun upsert(relationship: Relationship) {
        val existingVersion =
            database.relationshipQueries
                .selectById(relationship.id)
                .executeAsOneOrNull()
                ?.version ?: 0
        database.relationshipQueries.insertOrReplace(
            id = relationship.id,
            from_entity_type = relationship.from.entityType.name,
            from_entity_id = relationship.from.entityId,
            to_entity_type = relationship.to.entityType.name,
            to_entity_id = relationship.to.entityId,
            relationship_type = relationship.relationshipType.name,
            created_at = relationship.createdAt,
            updated_at = relationship.updatedAt,
            version = existingVersion + 1,
        )
    }

    override suspend fun delete(id: String) {
        database.relationshipQueries.deleteById(id)
    }
}
