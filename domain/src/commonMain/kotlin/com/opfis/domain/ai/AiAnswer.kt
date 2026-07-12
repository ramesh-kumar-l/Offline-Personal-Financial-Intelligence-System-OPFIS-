package com.opfis.domain.ai

import com.opfis.domain.entity.EntityType

/**
 * A composed answer from [LocalAiPort] (ROADMAP Phase 7, "Explainable
 * answers"). [citations] point back to the real records the answer was
 * derived from, so the UI can always show its work rather than present
 * an unverifiable claim.
 */
data class AiAnswer(
    val text: String,
    val citations: List<AiCitation> = emptyList(),
)

/** One record [AiAnswer.text] was derived from or references. */
data class AiCitation(
    val entityType: EntityType,
    val entityId: String,
    val label: String,
)
