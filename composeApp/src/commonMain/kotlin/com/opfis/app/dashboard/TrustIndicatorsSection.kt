package com.opfis.app.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.opfis.app.theme.OpfisColors
import com.opfis.domain.systemstatus.TrustIndicator
import com.opfis.domain.systemstatus.TrustIndicatorState

/**
 * Compact trust-indicator strip (SystemPrompt Part 3, "Trust
 * Indicators"), folded into the dashboard as the offline/encryption
 * health signal behind "What is my current financial health?".
 * Replaces the standalone Phase 0 `SystemStatusScreen`.
 */
@Composable
fun TrustIndicatorsSection(indicators: List<TrustIndicator>) {
    if (indicators.isEmpty()) return

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(bottom = 4.dp)) {
        indicators.forEach { indicator -> TrustIndicatorChip(indicator) }
    }
}

@Composable
private fun TrustIndicatorChip(indicator: TrustIndicator) {
    val tint =
        when (indicator.state) {
            TrustIndicatorState.ACTIVE -> OpfisColors.Success
            TrustIndicatorState.PENDING -> OpfisColors.Warning
        }
    val stateLabel =
        when (indicator.state) {
            TrustIndicatorState.ACTIVE -> "Active"
            TrustIndicatorState.PENDING -> "Pending"
        }

    Text(text = "● ${indicator.label}: $stateLabel", color = tint, style = MaterialTheme.typography.labelSmall)
}
