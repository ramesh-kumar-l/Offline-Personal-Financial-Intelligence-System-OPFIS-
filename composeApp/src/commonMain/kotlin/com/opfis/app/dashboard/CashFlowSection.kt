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
import com.opfis.app.dashboard.chart.CashFlowBarChart
import com.opfis.app.format.MoneyFormatter
import com.opfis.app.theme.OpfisColors
import com.opfis.domain.cashflow.CashFlowPeriod

/**
 * "Cash Flow" dashboard widget (SystemPrompt Part 3, "Charts": preferred
 * type). One card, one responsibility: monthly income vs. expense with
 * a legend (color is never the only signal for Income/Expense - both
 * carry a text label here and on every stat elsewhere in the app).
 */
@Composable
fun CashFlowSection(periods: List<CashFlowPeriod>) {
    val netMinorUnits = periods.sumOf { it.netMinorUnits }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Cash Flow", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "Net over ${periods.size} months: ${MoneyFormatter.format(netMinorUnits)}",
                style = MaterialTheme.typography.bodyMedium,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                LegendEntry(swatch = OpfisColors.Success, label = "Income")
                LegendEntry(swatch = OpfisColors.Error, label = "Expense")
            }

            if (periods.isEmpty() || periods.all { it.incomeMinorUnits == 0L && it.expenseMinorUnits == 0L }) {
                Text("No transactions recorded yet.", style = MaterialTheme.typography.bodySmall)
            } else {
                CashFlowBarChart(periods = periods, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun LegendEntry(
    swatch: Color,
    label: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(modifier = Modifier.size(10.dp).background(swatch, CircleShape))
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}
