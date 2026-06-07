package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.*
import com.example.viewmodel.*

@Composable
fun AlertCenterDrawer(
    alerts: List<AlertSignal>,
    customTriggers: List<UserCustomAlert>,
    onClose: () -> Unit,
    onDismissAlert: (String) -> Unit,
    onRemoveTrigger: (String) -> Unit,
    onClearAllAlerts: () -> Unit
) {
    var activeTab by remember { mutableStateOf(0) } // 0 = Live Signals, 1 = Configured Price Triggers

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onClose() }
    ) {
        // Absolute right drawer panel
        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.85f)
                .align(Alignment.CenterEnd)
                .clickable(enabled = false) { /* prevent prop */ },
            shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Drawer header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Alerts Hub",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(onClick = onClose) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close panel")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Alert segment tabs
                TabRow(
                    selectedTabIndex = activeTab,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Signals")
                                if (alerts.any { !it.isRead }) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(Color(0xFFEF4444), shape = CircleShape)
                                    )
                                }
                            }
                        }
                    )
                    Tab(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        text = { Text("My Triggers (${customTriggers.size})") }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Scrollable Alerts Content
                Box(modifier = Modifier.weight(1f)) {
                    if (activeTab == 0) {
                        // Live trading signals
                        if (alerts.isEmpty()) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "No live stock alerts triggered.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(onClick = onClearAllAlerts) {
                                        Text("Clear All")
                                    }
                                }

                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    items(alerts, key = { it.id }) { alert ->
                                        AlertCardElement(
                                            alert = alert,
                                            onRead = { onDismissAlert(alert.id) }
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // User customized price coordinates
                        if (customTriggers.isEmpty()) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "No price triggers scheduled yet.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "Tap on any stock grid and select \"Configure Price Alert\" to schedule.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(24.dp)
                                )
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(customTriggers, key = { it.id }) { trigger ->
                                    CustomTriggerCard(
                                        trigger = trigger,
                                        onRemove = { onRemoveTrigger(trigger.id) }
                                    )
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
fun AlertCardElement(
    alert: AlertSignal,
    onRead: () -> Unit
) {
    val levelColor = when (alert.level) {
        AlertLevel.URGENT -> Color(0xFFEF4444)
        AlertLevel.BREAKOUT -> Color(0xFFEAB308)
        AlertLevel.MOMENTUM -> Color(0xFF3B82F6)
        AlertLevel.INFO -> Color(0xFF10B981)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (alert.isRead) MaterialTheme.colorScheme.outlineVariant else levelColor.copy(alpha = 0.4f),
                RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (alert.isRead) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(levelColor, shape = CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = alert.ticker,
                        style = MaterialTheme.typography.titleSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (!alert.isRead) {
                    IconButton(onClick = onRead, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Mark as read",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = alert.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = alert.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CustomTriggerCard(
    trigger: UserCustomAlert,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = trigger.ticker,
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (trigger.exchangeIsNse) "NSE" else "NASDAQ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                val criteriaText = when (trigger.criteria) {
                    TechnicalAlertCriteria.PRICE_ABOVE -> "When Price goes ABOVE $${trigger.targetPrice}"
                    TechnicalAlertCriteria.PRICE_BELOW -> "When Price goes BELOW $${trigger.targetPrice}"
                    TechnicalAlertCriteria.CROSS_UP_EMA10 -> "Crossing UP 10 EMA"
                    TechnicalAlertCriteria.CROSS_UP_EMA20 -> "Crossing UP 20 EMA"
                    TechnicalAlertCriteria.CROSS_DOWN_EMA10 -> "Crossing DOWN 10 EMA"
                    TechnicalAlertCriteria.CROSS_DOWN_EMA20 -> "Crossing DOWN 20 EMA"
                    TechnicalAlertCriteria.CROSS_PREV_DAY_HIGH -> "Crossing Previous Day High"
                    TechnicalAlertCriteria.CROSS_PREV_WEEK_HIGH -> "Crossing Previous Week High"
                    TechnicalAlertCriteria.CROSS_PREV_MONTH_HIGH -> "Crossing Previous Month High"
                }
                Text(
                    text = "Condition: $criteriaText",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete price trigger",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
