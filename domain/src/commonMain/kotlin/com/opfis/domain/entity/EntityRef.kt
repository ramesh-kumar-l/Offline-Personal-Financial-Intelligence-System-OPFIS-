package com.opfis.domain.entity

import kotlinx.serialization.Serializable

/** A generic pointer to one entity row, identified by kind + id. */
@Serializable
data class EntityRef(
    val entityType: EntityType,
    val entityId: String,
)
