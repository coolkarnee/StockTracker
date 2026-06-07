package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.*
import com.example.viewmodel.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockDetailDialog(
    item: Any,   // IndustryPerformance, LiquidLeader, MomentumTopName, DoublerItem
    isNse: Boolean,
    onDismiss: () -> Unit,
    onAddAlert: (ticker: String, price: Double, criteria: TechnicalAlertCriteria) -> Unit
) {
    var showAddAlertForm by remember { mutableStateOf(false) }
    var alertPrice by remember { mutableStateOf("") }
    var selectedCriteria by remember { mutableStateOf(TechnicalAlertCriteria.PRICE_ABOVE) }
    var criteriaMenuExpanded by remember { mutableStateOf(false) }

    val criteriaOptions = listOf(
        TechnicalAlertCriteria.PRICE_ABOVE to "Price Cross Above",
        TechnicalAlertCriteria.PRICE_BELOW to "Price Cross Below",
        TechnicalAlertCriteria.CROSS_UP_EMA10 to "Crossing Up 10 EMA",
        TechnicalAlertCriteria.CROSS_UP_EMA20 to "Crossing Up 20 EMA",
        TechnicalAlertCriteria.CROSS_DOWN_EMA10 to "Crossing Down 10 EMA",
        TechnicalAlertCriteria.CROSS_DOWN_EMA20 to "Crossing Down 20 EMA",
        TechnicalAlertCriteria.CROSS_PREV_DAY_HIGH to "Crossing Previous Day High",
        TechnicalAlertCriteria.CROSS_PREV_WEEK_HIGH to "Crossing Previous Week High",
        TechnicalAlertCriteria.CROSS_PREV_MONTH_HIGH to "Crossing Previous Month High"
    )

    // Helper properties mapped based on object type
    val ticker = when (item) {
        is IndustryPerformance -> item.industryName
        is LiquidLeader -> item.ticker
        is MomentumTopName -> item.ticker
        is DoublerItem -> item.ticker
        else -> ""
    }

    val name = when (item) {
        is IndustryPerformance -> "Sector / Segment Performance"
        is LiquidLeader -> item.name
        is MomentumTopName -> item.name
        is DoublerItem -> item.name
        else -> ""
    }

    val priceStr = when (item) {
        is IndustryPerformance -> "Score: ${item.score}"
        is LiquidLeader -> if (isNse) "₹${String.format("%,.2f", item.lastPrice)}" else "\$${String.format("%,.2f", item.lastPrice)}"
        is MomentumTopName -> if (isNse) "₹${String.format("%,.2f", item.lastPrice)}" else "\$${String.format("%,.2f", item.lastPrice)}"
        is DoublerItem -> if (isNse) "₹${String.format("%,.2f", item.lastPrice)}" else "\$${String.format("%,.2f", item.lastPrice)}"
        else -> ""
    }

    val priceDouble = when (item) {
        is IndustryPerformance -> item.score
        is LiquidLeader -> item.lastPrice
        is MomentumTopName -> item.lastPrice
        is DoublerItem -> item.lastPrice
        else -> 0.0
    }

    val change = when (item) {
        is IndustryPerformance -> item.performance1M
        is LiquidLeader -> item.changePercent
        is MomentumTopName -> item.changePercent
        is DoublerItem -> item.changePercent
        else -> 0.0
    }

    val isPositive = change >= 0.0
    val accentColor = if (isPositive) Color(0xFF10B981) else Color(0xFFEF4444)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = ticker,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close dialog")
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)

                // Main body scrollable
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Pricing Display Card
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = priceStr,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isPositive) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${if (change >= 0) "+" else ""}${String.format("%.2f", change)}%",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = accentColor
                                )
                                Text(
                                    text = " (1M/Current Chg)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Sparkline visual expanded
                        LargeTrendChart(
                            modifier = Modifier
                                .width(120.dp)
                                .height(50.dp),
                            changePercent = change
                        )
                    }

                    if (item !is IndustryPerformance) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Interactive Technical Chart",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        TechnicalChart(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(320.dp),
                            ticker = ticker,
                            lastPrice = priceDouble,
                            changePercent = change
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 52 Week Proximity (for stock items only)
                    if (item !is IndustryPerformance) {
                        val (low, high) = when (item) {
                            is LiquidLeader -> Pair(priceDouble * 0.8, priceDouble * 1.05) // Simulated bounds
                            is MomentumTopName -> Pair(priceDouble * 0.75, priceDouble * 1.08)
                            is DoublerItem -> Pair(item.low52W, item.high52W)
                            else -> Pair(0.0, 0.0)
                        }

                        if (low > 0.0 && high > 0.0) {
                            Text(
                                text = "52-Week Price Spectrum Range",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isNse) "₹${String.format("%.2f", low)}" else "\$${String.format("%.2f", low)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                // Progress Slider Representation
                                val ratio = ((priceDouble - low) / (high - low)).coerceIn(0.0, 1.0).toFloat()
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 12.dp)
                                        .height(8.dp)
                                        .background(
                                            MaterialTheme.colorScheme.surfaceVariant,
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(ratio)
                                            .background(accentColor, shape = RoundedCornerShape(4.dp))
                                    )
                                }

                                Text(
                                    text = if (isNse) "₹${String.format("%.2f", high)}" else "\$${String.format("%.2f", high)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    // Key Metrics Matrix Grid
                    Text(
                        text = "Technical Metrics Dashboard",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        when (item) {
                            is IndustryPerformance -> {
                                MetricCell(Modifier.weight(1f), "1W Perf", "${if (item.performance1W >= 0) "+" else ""}${item.performance1W}%")
                                MetricCell(Modifier.weight(1f), "3M Perf", "${if (item.performance3M >= 0) "+" else ""}${item.performance3M}%")
                                MetricCell(Modifier.weight(1f), "Bulls/Bears", "${item.advancingStocks} / ${item.decliningStocks}")
                            }
                            is LiquidLeader -> {
                                MetricCell(Modifier.weight(1f), "Momentum Rank", "#${item.momentumRank}")
                                MetricCell(Modifier.weight(1f), "Rel Strength", "${item.relativeStrength}/100")
                                MetricCell(Modifier.weight(1f), "Volume", item.volume)
                            }
                            is MomentumTopName -> {
                                MetricCell(Modifier.weight(1f), "Daily Change", "${if (item.changePercent >= 0) "+" else ""}${item.changePercent}%")
                                MetricCell(Modifier.weight(1f), "Breakout Status", item.breakoutStatus)
                                MetricCell(Modifier.weight(1f), "Momentum Score", "${item.momentumScore}")
                            }
                            is DoublerItem -> {
                                MetricCell(Modifier.weight(1f), "PE Ratio", if (item.peRatio <= 0) "--" else "${item.peRatio}x")
                                MetricCell(Modifier.weight(1f), "Cap Type", item.marketCapType)
                                MetricCell(Modifier.weight(1f), "Growth Rate", "+${item.growthRate}%")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Alert Form Section
                    if (item !is IndustryPerformance) {
                        if (!showAddAlertForm) {
                            Button(
                                onClick = {
                                    alertPrice = String.format("%.2f", priceDouble)
                                    showAddAlertForm = true
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Notifications, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Configure Live Price Alert")
                            }
                        } else {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Setup Alert Trigger",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                            text = "Trigger Criteria",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Box(modifier = Modifier.fillMaxWidth()) {
                                            OutlinedButton(
                                                onClick = { criteriaMenuExpanded = true },
                                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = criteriaOptions.find { it.first == selectedCriteria }?.second ?: "Price Cross Above",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = null)
                                                }
                                            }
                                            DropdownMenu(
                                                expanded = criteriaMenuExpanded,
                                                onDismissRequest = { criteriaMenuExpanded = false },
                                                modifier = Modifier.fillMaxWidth(0.9f)
                                            ) {
                                                criteriaOptions.forEach { (option, label) ->
                                                    DropdownMenuItem(
                                                        text = { Text(label, style = MaterialTheme.typography.bodyMedium) },
                                                        onClick = {
                                                            selectedCriteria = option
                                                            criteriaMenuExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))

                                        if (selectedCriteria == TechnicalAlertCriteria.PRICE_ABOVE || selectedCriteria == TechnicalAlertCriteria.PRICE_BELOW) {
                                            OutlinedTextField(
                                                value = alertPrice,
                                                onValueChange = { alertPrice = it },
                                                label = { Text("Target Threshold Price") },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                                    .padding(12.dp)
                                            ) {
                                                Text(
                                                    text = "⚡ Real-time Smart Scan: This condition is assessed automatically against active price bars. You will be messaged contextually on trigger.",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextButton(onClick = { showAddAlertForm = false }) {
                                            Text("Cancel")
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = {
                                                val parsedPrice = if (selectedCriteria == TechnicalAlertCriteria.PRICE_ABOVE || selectedCriteria == TechnicalAlertCriteria.PRICE_BELOW) {
                                                    alertPrice.toDoubleOrNull() ?: priceDouble
                                                } else {
                                                    priceDouble
                                                }
                                                onAddAlert(ticker, parsedPrice, selectedCriteria)
                                                showAddAlertForm = false
                                            },
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text("Create Trigger")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LargeTrendChart(
    modifier: Modifier = Modifier,
    changePercent: Double
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val isPositive = changePercent >= 0.0
        val strokeColor = if (isPositive) Color(0xFF10B981) else Color(0xFFEF4444)

        val path = Path()
        val stepX = width / 6
        val points = floatArrayOf(
            height * 0.7f,
            height * 0.62f,
            height * 0.68f,
            height * 0.45f,
            height * 0.52f,
            height * 0.3f,
            if (isPositive) height * 0.15f else height * 0.85f
        )

        for (i in points.indices) {
            val x = i * stepX
            val y = points[i]
            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = strokeColor,
            style = Stroke(width = 3.dp.toPx())
        )
    }
}

@Composable
fun MetricCell(
    modifier: Modifier = Modifier,
    label: String,
    value: String
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
