package com.opfis.app.dashboard.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.opfis.app.format.MonthLabelFormatter
import com.opfis.app.theme.OpfisColors
import com.opfis.domain.cashflow.CashFlowPeriod

/**
 * Cash Flow bar chart (SystemPrompt Part 3, "Charts": preferred type).
 * One grouped bar per month: Income (fixed categorical color 1) and
 * Expense (fixed categorical color 2), never reassigned. Flat fills,
 * no gradients/animation/3D.
 */
@Composable
fun CashFlowBarChart(
    periods: List<CashFlowPeriod>,
    modifier: Modifier = Modifier,
) {
    val maxValue = periods.maxOfOrNull { maxOf(it.incomeMinorUnits, it.expenseMinorUnits) }?.coerceAtLeast(1L) ?: 1L
    val density = LocalDensity.current
    val barCornerRadius = with(density) { 4.dp.toPx() }
    val gapPx = with(density) { BAR_GAP_DP.dp.toPx() }

    Column(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxWidth().height(CHART_HEIGHT_DP.dp)) {
            if (periods.isEmpty()) return@Canvas
            val groupWidth = size.width / periods.size
            val barWidth = (groupWidth - gapPx * 3) / 2

            periods.forEachIndexed { index, period ->
                val groupStart = index * groupWidth + gapPx
                val geometry = BarGeometry(barWidth, size.height, barCornerRadius)
                drawBar(
                    color = OpfisColors.Success,
                    x = groupStart,
                    value = period.incomeMinorUnits,
                    maxValue = maxValue,
                    geometry = geometry,
                )
                drawBar(
                    color = OpfisColors.Error,
                    x = groupStart + barWidth + gapPx,
                    value = period.expenseMinorUnits,
                    maxValue = maxValue,
                    geometry = geometry,
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            periods.forEach { period ->
                Text(
                    text = MonthLabelFormatter.abbreviate(period.month),
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

private data class BarGeometry(
    val barWidth: Float,
    val chartHeight: Float,
    val cornerRadius: Float,
)

private fun DrawScope.drawBar(
    color: Color,
    x: Float,
    value: Long,
    maxValue: Long,
    geometry: BarGeometry,
) {
    val barHeight = if (value <= 0L) 0f else (value.toFloat() / maxValue.toFloat()) * geometry.chartHeight
    if (barHeight <= 0f) return
    drawRoundRect(
        color = color,
        topLeft = Offset(x, geometry.chartHeight - barHeight),
        size = Size(geometry.barWidth, barHeight),
        cornerRadius = CornerRadius(geometry.cornerRadius, geometry.cornerRadius),
    )
}

private const val CHART_HEIGHT_DP = 140
private const val BAR_GAP_DP = 4
