package com.opfis.domain.ai

import com.opfis.domain.entity.EntityType

/** One record surfaced by [RetrieveFinancialContextUseCase]'s semantic-retrieval layer. */
data class RetrievedItem(
    val entityType: EntityType,
    val entityId: String,
    val summary: String,
)
