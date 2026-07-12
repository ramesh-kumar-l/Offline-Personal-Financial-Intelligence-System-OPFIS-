package com.opfis.data.relationship

import com.opfis.domain.entity.EntityRef
import com.opfis.domain.entity.EntityType
import com.opfis.domain.relationship.Relationship
import com.opfis.domain.relationship.RelationshipType
import com.opfis.data.db.Relationship as RelationshipRow

internal fun toDomainRelationship(row: RelationshipRow): Relationship =
    Relationship(
        id = row.id,
        from = EntityRef(EntityType.valueOf(row.from_entity_type), row.from_entity_id),
        to = EntityRef(EntityType.valueOf(row.to_entity_type), row.to_entity_id),
        relationshipType = RelationshipType.valueOf(row.relationship_type),
        createdAt = row.created_at,
        updatedAt = row.updated_at,
    )
