package com.opfis.domain.entity

/** A generic pointer to one entity row, identified by kind + id. */
data class EntityRef(
    val entityType: EntityType,
    val entityId: String,
)
