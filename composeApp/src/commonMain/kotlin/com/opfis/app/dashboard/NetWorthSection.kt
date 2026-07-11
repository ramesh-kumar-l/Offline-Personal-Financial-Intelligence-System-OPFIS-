package com.opfis.app.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.opfis.app.dashboard.chart.AssetAllocationDonutChart
import com.opfis.app.format.MoneyFormatter
import com.opfis.app.theme.OpfisColors
import com.opfis.domain.networth.NetWorthSummary

/**
 * "Net Worth" dashboard widget (SystemPrompt Part 3, "Home Dashboard").
 * One card, one responsibility: the headline number, its Assets/
 * Liabilities components (color always paired with a label - never
 * color alone), and the Asset Allocation donut breaking assets down
 * into Accounts vs. Other Assets.
 */
@Composable
fun NetWorthSection(summary: NetWorthSummary) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Net Worth", style = MaterialTheme.typography.titleMedium)
            Text(MoneyFormatter.format(summary.netWorthMinorUnits), style = MaterialTheme.typography.headlineMedium)

            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                NetWorthStat(
                    label = "Assets",
                    amountMinorUnits = summary.totalAssetsMinorUnits,
                    tint = OpfisColors.Success,
                )
                NetWorthStat(
                    label = "Liabilities",
                    amountMinorUnits = summary.liabilityBalanceMinorUnits,
                    tint = OpfisColors.Error,
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                AssetAllocationDonutChart(
                    accountBalanceMinorUnits = summary.accountBalanceMinorUnits,
                    assetValueMinorUnits = summary.assetValueMinorUnits,
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AllocationLegendRow(
                        swatch = OpfisColors.ProfessionalBlue,
                        label = "Accounts",
                        amountMinorUnits = summary.accountBalanceMinorUnits,
                    )
                    AllocationLegendRow(
                        swatch = OpfisColors.InformationNeutralBlue,
                        label = "Other Assets",
                        amountMinorUnits = summary.assetValueMinorUnits,
                    )
                }
            }
        }
    }
}

@Composable
private fun NetWorthStat(
    label: String,
    amountMinorUnits: Long,
    tint: Color,
) {
    Column {
        Text(label, style = MaterialTheme.typography.bodySmall)
        Text(MoneyFormatter.format(amountMinorUnits), style = MaterialTheme.typography.bodyLarge, color = tint)
    }
}

@Composable
private fun AllocationLegendRow(
    swatch: Color,
    label: String,
    amountMinorUnits: Long,
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(modifier = Modifier.size(10.dp).background(swatch, CircleShape))
        Text("$label: ${MoneyFormatter.format(amountMinorUnits)}", style = MaterialTheme.typography.bodySmall)
    }
}
