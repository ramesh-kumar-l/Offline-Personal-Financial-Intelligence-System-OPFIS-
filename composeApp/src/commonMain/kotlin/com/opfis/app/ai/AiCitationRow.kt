package com.opfis.app.ai

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.opfis.app.theme.OpfisColors
import com.opfis.domain.ai.AiCitation
import com.opfis.domain.entity.EntityType

/**
 * Renders one [AiCitation] so an [com.opfis.domain.ai.AiAnswer] always
 * shows its work (ROADMAP Phase 7, "Explainable answers"). Every entity
 * kind pairs a text prefix with a fixed color - never color alone,
 * following [com.opfis.app.search.SearchResultRow]'s convention.
 */
@Composable
internal fun AiCitationRow(citation: AiCitation) {
    val tint =
        when (citation.entityType) {
            EntityType.ACCOUNT, EntityType.TRANSACTION -> OpfisColors.ProfessionalBlue
            EntityType.CATEGORY, EntityType.DOCUMENT -> OpfisColors.InformationNeutralBlue
            EntityType.TAG, EntityType.MEMORY_EVENT, EntityType.BUDGET -> OpfisColors.Warning
            EntityType.ASSET, EntityType.GOAL -> OpfisColors.Success
            EntityType.LIABILITY -> OpfisColors.Error
        }
    Text("· ${citation.entityType.name}: ${citation.label}", style = MaterialTheme.typography.bodySmall, color = tint)
}
