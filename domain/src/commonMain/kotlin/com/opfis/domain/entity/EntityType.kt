package com.opfis.domain.entity

import kotlinx.serialization.Serializable

/**
 * Every addressable domain entity kind (ROADMAP Phase 6, "Knowledge
 * graph abstractions") - used to reference an entity generically from
 * [com.opfis.domain.memory.MemoryEvent.subject] and
 * [com.opfis.domain.relationship.Relationship] without those packages
 * depending on every feature package directly. Deliberately distinct
 * from [com.opfis.domain.search.SearchEntityType], which only lists
 * FTS5-indexed kinds - this enum also covers entities with no search
 * presence (Asset, Liability, Budget, Goal).
 */
@Serializable
enum class EntityType {
    ACCOUNT,
    ASSET,
    LIABILITY,
    CATEGORY,
    TRANSACTION,
    BUDGET,
    GOAL,
    DOCUMENT,
    TAG,
    MEMORY_EVENT,
}
