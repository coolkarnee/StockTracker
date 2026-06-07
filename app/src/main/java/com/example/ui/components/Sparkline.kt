package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun Sparkline(
    modifier: Modifier = Modifier,
    changePercent: Double,
    pointsCount: Int = 12
) {
    // Generate a beautiful, stable mock chart path based on the stock change percent to depict realistic fluctuations
    val points = remember(changePercent, pointsCount) {
        val list = mutableListOf<Float>()
        var current = 50f
        list.add(current)
        val trend = (changePercent / 3.0).toFloat()
        // Simple random walk seeded by change percent to make the chart line visually map to the actual gain/loss
        val rand = java.util.Random(changePercent.toLong().coerceAtLeast(1L))
        for (i in 1 until pointsCount) {
            val step = (rand.nextFloat() - 0.45f) * 20f + trend * 4f
            current = (current - step).coerceIn(10f, 95f)
            list.add(current)
        }
        list
    }

    val isPositive = changePercent >= 0.0
    val strokeColor = if (isPositive) Color(0xFF10B981) else Color(0xFFEF4444)
    val glowColor = if (isPositive) Color(0x3310B981) else Color(0x33EF4444)

    Canvas(
        modifier = modifier
            .width(80.dp)
            .height(35.dp)
    ) {
        val width = size.width
        val height = size.height

        val path = Path()
        val fillPath = Path()

        val stepX = width / (points.size - 1)
        val maxVal = 100f
        val minVal = 0f
        val valRange = maxVal - minVal

        points.forEachIndexed { index, value ->
            val x = index * stepX
            val y = height - ((value - minVal) / valRange * height)

            if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, height)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
            if (index == points.size - 1) {
                fillPath.lineTo(x, height)
                fillPath.close()
            }
        }

        // Draw background gradient fill under the line
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(glowColor, Color.Transparent),
                startY = 0f,
                endY = height
            )
        )

        // Draw sparkline itself
        drawPath(
            path = path,
            color = strokeColor,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}
