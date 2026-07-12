package com.opfis.domain.relationship

import com.opfis.domain.entity.EntityRef

/**
 * A user-declared, typed link between two entities (ROADMAP Phase 6,
 * "Relationships") - e.g. a loan Document supporting a Liability, or a
 * Goal a set of Accounts contributes to. Deliberately separate from
 * relationships already expressed as foreign keys elsewhere
 * (`Document.linkedTransactionId`, the `transaction_tag` join table,
 * `Category.parentId`) - those remain each feature's own concern; this
 * table only stores links a user explicitly draws that the schema has
 * no dedicated column for.
 */
data class Relationship(
    val id: String,
    val from: EntityRef,
    val to: EntityRef,
    val relationshipType: RelationshipType,
    val createdAt: Long,
    val updatedAt: Long,
) {
    init {
        require(from != to) { "a relationship cannot link an entity to itself" }
    }
}

enum class RelationshipType {
    RELATED,
    SUPPORTING_DOCUMENT,
    CONTRIBUTES_TO,
    PART_OF,
}
