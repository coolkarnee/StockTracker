package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import kotlin.math.roundToInt

@OptIn(ExperimentalTextApi::class)
@Composable
fun TechnicalChart(
    modifier: Modifier = Modifier,
    ticker: String,
    lastPrice: Double,
    changePercent: Double
) {
    // Interactive toggles for indicators
    var showEma10 by remember { mutableStateOf(true) }
    var showEma20 by remember { mutableStateOf(true) }
    var showEma50 by remember { mutableStateOf(true) }
    var showVolEma by remember { mutableStateOf(true) }
    
    // Timeframe selector
    var activeTimeframe by remember { mutableStateOf("1D") }
    
    // Auto-generate realistic historical candle data based on current price
    val candles = remember(ticker, activeTimeframe) {
        val count = 80
        val volatility = when (activeTimeframe) {
            "1H" -> 0.007
            "1D" -> 0.021
            "1W" -> 0.052
            else -> 0.021
        }
        TechnicalUtils.generateCandleData(lastPrice, changePercent, count, volatility)
    }

    // Calculating Exponential Moving Averages (EMAs)
    val ema10Values = remember(candles) { TechnicalUtils.calculateEMA(candles.map { it.close }, 10) }
    val ema20Values = remember(candles) { TechnicalUtils.calculateEMA(candles.map { it.close }, 20) }
    val ema50Values = remember(candles) { TechnicalUtils.calculateEMA(candles.map { it.close }, 50) }
    val emaVolume20Values = remember(candles) { TechnicalUtils.calculateEMA(candles.map { it.volume }, 20) }

    // Display state for hover/crosshair tracking
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var touchPointX by remember { mutableStateOf<Float?>(null) }
    var touchPointY by remember { mutableStateOf<Float?>(null) }

    // Use selected index if hovering, otherwise default to the latest candle
    val currentDisplayIndex = selectedIndex ?: (candles.size - 1)
    val displayCandle = candles.getOrNull(currentDisplayIndex) ?: candles.last()
    
    val currentEma10 = ema10Values.getOrNull(currentDisplayIndex)
    val currentEma20 = ema20Values.getOrNull(currentDisplayIndex)
    val currentEma50 = ema50Values.getOrNull(currentDisplayIndex)
    val currentEmaVol20 = emaVolume20Values.getOrNull(currentDisplayIndex)

    val detailTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val greenColor = Color(0xFF10B981) // Bullish Green
    val redColor = Color(0xFFEF4444)   // Bearish Red
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0F172A), RoundedCornerShape(16.dp)) // Deep slate black
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        // TOP BAR: Tab Period Selector and Indicator toggles
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Period chips
            Row(
                modifier = Modifier
                    .background(Color(0xFF1E293B), RoundedCornerShape(20.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("1H", "1D", "1W").forEach { period ->
                    val isSelected = activeTimeframe == period
                    Box(
                        modifier = Modifier
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { activeTimeframe = period }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = period,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Indicator checkboxes
            Row(
                modifier = Modifier.padding(start = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IndicatorToggleChip(
                    label = "EMA 10",
                    tint = Color(0xFF38BDF8),
                    isSelected = showEma10,
                    onClick = { showEma10 = !showEma10 }
                )
                IndicatorToggleChip(
                    label = "EMA 20",
                    tint = Color(0xFF818CF8),
                    isSelected = showEma20,
                    onClick = { showEma20 = !showEma20 }
                )
                IndicatorToggleChip(
                    label = "EMA 50",
                    tint = Color(0xFFF59E0B),
                    isSelected = showEma50,
                    onClick = { showEma50 = !showEma50 }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // HOVER/LATEST DATA HUD PANEL
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E293B).copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            // O,H,L,C
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = displayCandle.label + (if (selectedIndex != null) " (Hover)" else ""),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OhlcValueText("O", displayCandle.open)
                    OhlcValueText("H", displayCandle.high)
                    OhlcValueText("L", displayCandle.low)
                    OhlcValueText("C", displayCandle.close, color = if (displayCandle.close >= displayCandle.open) greenColor else redColor)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Indicators Info Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "V: " + String.format("%,.0f", displayCandle.volume),
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = detailTextColor
                )

                if (showEma10 && currentEma10 != null) {
                    IndicatorMetricBadge("EMA(10)", currentEma10, Color(0xFF38BDF8))
                }
                if (showEma20 && currentEma20 != null) {
                    IndicatorMetricBadge("EMA(20)", currentEma20, Color(0xFF818CF8))
                }
                if (showEma50 && currentEma50 != null) {
                    IndicatorMetricBadge("EMA(50)", currentEma50, Color(0xFFF59E0B))
                }
                if (showVolEma && currentEmaVol20 != null) {
                    IndicatorMetricBadge("VEma(20)", currentEmaVol20, Color(0xFFEC4899))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // MAIN COMBINED CHART CANVAS
        val textMeasurer = rememberTextMeasurer()
        val textStyle = TextStyle(
            color = Color(0xFF64748B),
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace
        )
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF030712)) // Dark graph grid area
                .pointerInput(candles) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            selectedIndex = findCandleIndexByX(offset.x, size.width.toFloat(), candles.size)
                            touchPointX = offset.x
                            touchPointY = offset.y
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val curX = change.position.x
                            selectedIndex = findCandleIndexByX(curX, size.width.toFloat(), candles.size)
                            touchPointX = curX
                            touchPointY = change.position.y
                        },
                        onDragEnd = {
                            selectedIndex = null
                            touchPointX = null
                            touchPointY = null
                        }
                    )
                }
                .pointerInput(candles) {
                    detectTapGestures(
                        onTap = { offset ->
                            selectedIndex = findCandleIndexByX(offset.x, size.width.toFloat(), candles.size)
                            touchPointX = offset.x
                            touchPointY = offset.y
                        }
                    )
                }
        ) {
            val primaryThemeColor = MaterialTheme.colorScheme.primary
            Canvas(modifier = Modifier.fillMaxSize()) {
                val chartWidth = size.width
                val chartHeight = size.height

                // Allocating space:
                // Upper 70% is Price Candlestick grid
                // Middle 5% is space
                // Lower 25% is Volume grid
                val priceChartHeight = chartHeight * 0.70f
                val volumeChartHeight = chartHeight * 0.25f
                val volumeChartYStart = chartHeight * 0.75f

                val paddingRight = 60.dp.toPx() // price scale space
                val activeWidth = chartWidth - paddingRight

                val visibleCount = candles.size
                if (visibleCount == 0) return@Canvas

                val candleWidth = activeWidth / visibleCount

                // Find mins and maxs for scaling
                val visiblePrices = candles.flatMap { listOf(it.high, it.low) }.toMutableList()
                if (showEma10) visiblePrices.addAll(ema10Values.filterNotNull())
                if (showEma20) visiblePrices.addAll(ema20Values.filterNotNull())
                if (showEma50) visiblePrices.addAll(ema50Values.filterNotNull())

                val priceMax = (visiblePrices.maxOrNull() ?: 1.0) * 1.002
                val priceMin = (visiblePrices.minOrNull() ?: 0.0) * 0.998
                val priceRange = maxOf(priceMax - priceMin, 0.01)

                val volMax = candles.maxOf { it.volume } * 1.05
                val volRange = if (volMax > 0) volMax else 1000.0

                // Helper lambda to calculate screen Y from price
                val getPriceY = { p: Double ->
                    val frac = (p - priceMin) / priceRange
                    (priceChartHeight * (1.0 - frac)).toFloat()
                }

                // Helper lambda to calculate screen Y from volume
                val getVolY = { v: Double ->
                    val frac = v / volRange
                    (volumeChartYStart + volumeChartHeight * (1.0 - frac)).toFloat()
                }

                // DRAW HORIZONTAL PRICE GRID LINES (4 steps)
                val gridLinesCount = 4
                for (j in 0..gridLinesCount) {
                    val p = priceMin + (priceRange / gridLinesCount) * j
                    val screenY = getPriceY(p)
                    drawLine(
                        color = Color(0xFF1E293B),
                        start = Offset(0f, screenY),
                        end = Offset(activeWidth, screenY),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    )
                    
                    // Draw right y-axis values
                    val textLayout = textMeasurer.measure(
                        text = String.format("%.2f", p),
                        style = textStyle
                    )
                    drawText(
                        textLayoutResult = textLayout,
                        topLeft = Offset(activeWidth + 4.dp.toPx(), screenY - textLayout.size.height / 2f)
                    )
                }

                // DRAW VOLUME HORIZONTAL GRID (1 line)
                val halfVolY = getVolY(volMax / 2.0)
                drawLine(
                    color = Color(0xFF1E293B),
                    start = Offset(0f, halfVolY),
                    end = Offset(activeWidth, halfVolY),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                )

                // DRAW CANDLESTICKS & VOLUME BARS
                for (idx in candles.indices) {
                    val candle = candles[idx]
                    val centerX = idx * candleWidth + candleWidth / 2f

                    // Price positions
                    val openY = getPriceY(candle.open)
                    val closeY = getPriceY(candle.close)
                    val highY = getPriceY(candle.high)
                    val lowY = getPriceY(candle.low)

                    val isBullish = candle.close >= candle.open
                    val itemColor = if (isBullish) greenColor else redColor

                    // 1. Draw Candle Wick (high to low)
                    drawLine(
                        color = itemColor,
                        start = Offset(centerX, highY),
                        end = Offset(centerX, lowY),
                        strokeWidth = 1.5.dp.toPx()
                    )

                    // 2. Draw Candle Body
                    val bodyHeight = kotlin.math.abs(closeY - openY).coerceAtLeast(1.dp.toPx())
                    val bodyY = minOf(openY, closeY)
                    val bodyWidth = (candleWidth * 0.75f).coerceAtLeast(2.dp.toPx())

                    drawRect(
                        color = itemColor,
                        topLeft = Offset(centerX - bodyWidth / 2f, bodyY),
                        size = Size(bodyWidth, bodyHeight)
                    )

                    // 3. Draw Volume Bar
                    val volY = getVolY(candle.volume)
                    val volBarHeight = chartHeight - volY
                    drawRect(
                        color = itemColor.copy(alpha = 0.5f),
                        topLeft = Offset(centerX - bodyWidth / 2f, volY),
                        size = Size(bodyWidth, volBarHeight)
                    )
                }

                // DRAW EMA LINES (OVER MAIN PRICE CANVAS)
                if (showEma10) {
                    drawEmaLine(candles, ema10Values, candleWidth, Color(0xFF38BDF8), getPriceY)
                }
                if (showEma20) {
                    drawEmaLine(candles, ema20Values, candleWidth, Color(0xFF818CF8), getPriceY)
                }
                if (showEma50) {
                    drawEmaLine(candles, ema50Values, candleWidth, Color(0xFFF59E0B), getPriceY)
                }

                // DRAW VOLUME EMA LINE (OVER VOLUME CANVAS)
                if (showVolEma) {
                    drawEmaLine(candles, emaVolume20Values, candleWidth, Color(0xFFEC4899), getVolY)
                }

                // TIME LABELS AT THE BOTTOM (At 3 sections)
                val step = visibleCount / 3
                for (j in 0..2) {
                    val idx = (j * step).coerceAtMost(visibleCount - 1)
                    val candle = candles[idx]
                    val tX = idx * candleWidth + candleWidth / 2f
                    
                    val dateText = textMeasurer.measure(
                        text = candle.label,
                        style = textStyle
                    )
                    drawText(
                        textLayoutResult = dateText,
                        topLeft = Offset(tX - dateText.size.width / 2f, chartHeight - dateText.size.height)
                    )
                }

                // DRAW INTERACTIVE CROSSHAIR CURSOR ON TOUCH/GESTURE
                touchPointX?.let { tx ->
                    val fitTx = tx.coerceIn(0f, activeWidth)
                    val touchIndex = findCandleIndexByX(fitTx, activeWidth, visibleCount)
                    val candle = candles.getOrNull(touchIndex)
                    
                    if (candle != null) {
                        val centerX = touchIndex * candleWidth + candleWidth / 2f
                        val actualTouchY = touchPointY ?: getPriceY(candle.close)

                        // Vertical tracking line
                        drawLine(
                            color = Color(0xFF94A3B8).copy(alpha = 0.8f),
                            start = Offset(centerX, 0f),
                            end = Offset(centerX, chartHeight),
                            strokeWidth = 1f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 5f))
                        )

                        // Horizontal tracking line
                        drawLine(
                            color = Color(0xFF94A3B8).copy(alpha = 0.8f),
                            start = Offset(0f, actualTouchY),
                            end = Offset(activeWidth, actualTouchY),
                            strokeWidth = 1f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 5f))
                        )

                        // Hover Highlight Circle on Candle close
                        drawCircle(
                            color = primaryThemeColor,
                            radius = 6.dp.toPx(),
                            center = Offset(centerX, getPriceY(candle.close)),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawEmaLine(
    candles: List<Candle>,
    emaValues: List<Double?>,
    candleWidth: Float,
    color: Color,
    getY: (Double) -> Float
) {
    val path = Path()
    var isStarted = false
    
    for (idx in candles.indices) {
        val valEma = emaValues[idx] ?: continue
        val centerX = idx * candleWidth + candleWidth / 2f
        val y = getY(valEma)
        
        if (!isStarted) {
            path.moveTo(centerX, y)
            isStarted = true
        } else {
            path.lineTo(centerX, y)
        }
    }
    
    if (isStarted) {
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 1.75.dp.toPx())
        )
    }
}

@Composable
fun OhlcValueText(label: String, value: Double, color: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    Row {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        Text(
            text = String.format("%.2f", value),
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color = color,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 2.dp)
        )
    }
}

@Composable
fun IndicatorMetricBadge(label: String, value: Double, tint: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(tint, shape = CircleShape)
        )
        Text(
            text = "$label: ${String.format("%.2f", value)}",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = tint
        )
    }
}

@Composable
fun IndicatorToggleChip(
    label: String,
    tint: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                if (isSelected) tint.copy(alpha = 0.25f) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                1.dp,
                if (isSelected) tint else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(tint, shape = CircleShape)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Interactive helper to find index under touch coordinates
fun findCandleIndexByX(x: Float, chartWidth: Float, visibleCount: Int): Int {
    val candleWidth = chartWidth / visibleCount
    val calculated = (x / candleWidth).roundToInt()
    return calculated.coerceIn(0, visibleCount - 1)
}
