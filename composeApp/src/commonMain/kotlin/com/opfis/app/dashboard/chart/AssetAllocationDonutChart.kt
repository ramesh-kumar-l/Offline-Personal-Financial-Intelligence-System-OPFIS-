package com.opfis.app.dashboard.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.opfis.app.theme.OpfisColors

/**
 * Asset Allocation donut (SystemPrompt Part 3, "Charts": preferred type).
 * Two categorical slices in a fixed order - Accounts, then Other Assets
 * - never cycled or reassigned. Flat fills only: no gradients, no 3D, no
 * animation (SystemPrompt Part 3, "Charts: Avoid").
 */
@Composable
fun AssetAllocationDonutChart(
    accountBalanceMinorUnits: Long,
    assetValueMinorUnits: Long,
    modifier: Modifier = Modifier,
) {
    val total = (accountBalanceMinorUnits + assetValueMinorUnits).coerceAtLeast(0L)

    Canvas(modifier = modifier.size(DIAMETER_DP.dp)) {
        val strokeWidth = size.minDimension * STROKE_FRACTION
        val arcDiameter = size.minDimension - strokeWidth
        val topLeft = Offset((size.width - arcDiameter) / 2f, (size.height - arcDiameter) / 2f)
        val arcSize = Size(arcDiameter, arcDiameter)

        if (total <= 0L) {
            drawArc(
                color = OpfisColors.BackgroundNeutralGray,
                startAngle = 0f,
                sweepAngle = FULL_SWEEP,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
            )
            return@Canvas
        }

        val accountsShare = accountBalanceMinorUnits.toFloat() / total.toFloat()
        val accountsSweep = (accountsShare * FULL_SWEEP).coerceIn(0f, FULL_SWEEP)
        val assetsSweep = FULL_SWEEP - accountsSweep
        val gap = if (accountsSweep > 0f && assetsSweep > 0f) GAP_DEGREES else 0f
        val bounds = Rect(topLeft, arcSize)

        drawSlice(
            color = OpfisColors.ProfessionalBlue,
            startAngle = -90f,
            sweepAngle = (accountsSweep - gap).coerceAtLeast(0f),
            bounds = bounds,
            strokeWidth = strokeWidth,
        )
        drawSlice(
            color = OpfisColors.InformationNeutralBlue,
            startAngle = -90f + accountsSweep,
            sweepAngle = (assetsSweep - gap).coerceAtLeast(0f),
            bounds = bounds,
            strokeWidth = strokeWidth,
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSlice(
    color: Color,
    startAngle: Float,
    sweepAngle: Float,
    bounds: Rect,
    strokeWidth: Float,
) {
    if (sweepAngle <= 0f) return
    drawArc(
        color = color,
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        useCenter = false,
        topLeft = bounds.topLeft,
        size = Size(bounds.width, bounds.height),
        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
    )
}

private const val DIAMETER_DP = 160
private const val STROKE_FRACTION = 0.22f
private const val FULL_SWEEP = 360f
private const val GAP_DEGREES = 3f
