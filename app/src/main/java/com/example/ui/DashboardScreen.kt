package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.*
import com.example.ui.components.*
import com.example.viewmodel.*
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: StockViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("SheetsConfig", android.content.Context.MODE_PRIVATE) }

    val customNseId by viewModel.customNseId.collectAsState()
    val customNasdaqId by viewModel.customNasdaqId.collectAsState()

    var showSheetsConfig by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val savedNse = sharedPrefs.getString("nseId", StockRepository.DEFAULT_NSE_ID) ?: StockRepository.DEFAULT_NSE_ID
        val savedNasdaq = sharedPrefs.getString("nasdaqId", StockRepository.DEFAULT_NASDAQ_ID) ?: StockRepository.DEFAULT_NASDAQ_ID
        viewModel.updateSpreadsheetIds(savedNse, savedNasdaq)
    }

    val isNse by viewModel.isNse.collectAsState()
    val activeSubTab by viewModel.currentSubTab.collectAsState()
    val syncState by viewModel.syncState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortBy by viewModel.sortBy.collectAsState()

    // Data streams
    val industries by viewModel.filteredIndustries.collectAsState()
    val liquidLeaders by viewModel.filteredLiquidLeaders.collectAsState()
    val momentumTopNames by viewModel.filteredMomentumTopNames.collectAsState()
    val doublers by viewModel.filteredDoublers.collectAsState()

    val activeScanCriteria by viewModel.selectedScanCriteria.collectAsState()
    val scannedStocks by viewModel.scannedStocks.collectAsState()

    val alerts by viewModel.alerts.collectAsState()
    val customTriggers by viewModel.customAlertTriggers.collectAsState()

    var activeDialogItem by remember { mutableStateOf<Any?>(null) }
    var isAlertDrawerOpen by remember { mutableStateOf(false) }
    var isSortMenuExpanded by remember { mutableStateOf(false) }

    val unreadAlertsCount = alerts.count { !it.isRead }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Upper Bar with Exchange Selector and Notifications Badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Exchange Selector tabs
                Row(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isNse) MaterialTheme.colorScheme.primary else Color.Transparent
                            )
                            .clickable { viewModel.setExchange(true) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "NSE (India)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isNse) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (!isNse) MaterialTheme.colorScheme.primary else Color.Transparent
                            )
                            .clickable { viewModel.setExchange(false) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "NASDAQ (US)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (!isNse) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Header Notification badge and Refresh actions
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { showSheetsConfig = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Configure Google Sheets Spreadsheet Source"
                        )
                    }

                    IconButton(
                        onClick = { viewModel.refreshData() }
                    ) {
                        if (isRefreshing) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh data feeds"
                            )
                        }
                    }

                    Box {
                        IconButton(
                            onClick = { isAlertDrawerOpen = true },
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.surface,
                                    shape = CircleShape
                                )
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Active alarms list",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (unreadAlertsCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-2).dp, y = 2.dp)
                                    .size(18.dp)
                                    .background(Color(0xFFEF4444), shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = unreadAlertsCount.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Sync Status Indicator Strip
            val statusColor = when (syncState) {
                SyncState.LIVE -> Color(0xFF10B981)
                SyncState.OFFLINE_CACHE -> Color(0xFFF59E0B)
                SyncState.SYNCING -> MaterialTheme.colorScheme.primary
                else -> Color.Gray
            }
            val statusText = when (syncState) {
                SyncState.LIVE -> "Connected to Google Sheets - Synchronized Live"
                SyncState.OFFLINE_CACHE -> "Offline Cache - Showing pre-seeded metrics"
                SyncState.SYNCING -> "Querying sheets performance data..."
                else -> "Awaiting feed connection status..."
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(statusColor, shape = CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sub-Tabs Navigation (Scrollable row)
            ScrollableTabRow(
                selectedTabIndex = activeSubTab,
                containerColor = Color.Transparent,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                val tabLabels = listOf("Ind Analysis", "Momentum Leaders", "Top Names", "Doublers", "Action Scans")
                tabLabels.forEachIndexed { i, label ->
                    Tab(
                        selected = activeSubTab == i,
                        onClick = { viewModel.setSubTab(i) },
                        text = {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (activeSubTab == i) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Live filter and sorting control bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search tickers, companies, industries...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Sorter Dropdown Button
                Box {
                    IconButton(
                        onClick = { isSortMenuExpanded = true },
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                    ) {
                        Icon(imageVector = Icons.Default.List, contentDescription = "Sort options list")
                    }

                    DropdownMenu(
                        expanded = isSortMenuExpanded,
                        onDismissRequest = { isSortMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Default Sorting") },
                            onClick = { viewModel.setSortBy(SortOption.DEFAULT); isSortMenuExpanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text("LTP High -> Low") },
                            onClick = { viewModel.setSortBy(SortOption.PRICE_DESC); isSortMenuExpanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text("LTP Low -> High") },
                            onClick = { viewModel.setSortBy(SortOption.PRICE_ASC); isSortMenuExpanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Daily Change %") },
                            onClick = { viewModel.setSortBy(SortOption.CHANGE_DESC); isSortMenuExpanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Momentum Rating") },
                            onClick = { viewModel.setSortBy(SortOption.SCORE_DESC); isSortMenuExpanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Volume Ratio") },
                            onClick = { viewModel.setSortBy(SortOption.VOLUME_DESC); isSortMenuExpanded = false }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Main Listing grid
            Box(modifier = Modifier.weight(1f)) {
                when (activeSubTab) {
                    0 -> {
                        // Tab 1: Industry Analysis
                        if (industries.isEmpty()) {
                            EmptyStatePlaceholder("No matching industries found.")
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                itemsIndexed(industries) { _, item ->
                                    IndustryAnalysisCard(
                                        item = item,
                                        onClick = { activeDialogItem = item }
                                    )
                                }
                            }
                        }
                    }
                    1 -> {
                        // Tab 2: Liquid Momentum Leaders
                        if (liquidLeaders.isEmpty()) {
                            EmptyStatePlaceholder("No liquid momentum leaders found.")
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                itemsIndexed(liquidLeaders) { _, item ->
                                    LiquidLeaderCard(
                                        item = item,
                                        isNse = isNse,
                                        onClick = { activeDialogItem = item }
                                    )
                                }
                            }
                        }
                    }
                    2 -> {
                        // Tab 3: Momentum Top Names
                        if (momentumTopNames.isEmpty()) {
                            EmptyStatePlaceholder("No matching momentum stocks found.")
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                itemsIndexed(momentumTopNames) { _, item ->
                                    MomentumTopNameCard(
                                        item = item,
                                        isNse = isNse,
                                        onClick = { activeDialogItem = item }
                                    )
                                }
                            }
                        }
                    }
                    3 -> {
                        // Tab 4: Doublers High Potentials
                        if (doublers.isEmpty()) {
                            EmptyStatePlaceholder("No matching doublers list found.")
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                itemsIndexed(doublers) { _, item ->
                                    DoublerCard(
                                        item = item,
                                        isNse = isNse,
                                        onClick = { activeDialogItem = item }
                                    )
                                }
                            }
                        }
                    }
                    4 -> {
                        // Tab 5: Action Scans
                        Column(modifier = Modifier.fillMaxSize()) {
                            val scannerOptions = listOf(
                                TechnicalAlertCriteria.CROSS_UP_EMA10 to "UP 10 EMA",
                                TechnicalAlertCriteria.CROSS_UP_EMA20 to "UP 20 EMA",
                                TechnicalAlertCriteria.CROSS_DOWN_EMA10 to "DOWN 10 EMA",
                                TechnicalAlertCriteria.CROSS_DOWN_EMA20 to "DOWN 20 EMA",
                                TechnicalAlertCriteria.CROSS_PREV_DAY_HIGH to "Prev Day High",
                                TechnicalAlertCriteria.CROSS_PREV_WEEK_HIGH to "Prev Week High",
                                TechnicalAlertCriteria.CROSS_PREV_MONTH_HIGH to "Prev Month High"
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                scannerOptions.forEach { (option, label) ->
                                    val isSelected = activeScanCriteria == option
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.setScanCriteria(option) },
                                        label = {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    )
                                }
                            }

                            if (scannedStocks.isEmpty()) {
                                EmptyStatePlaceholder("No live tickers matched this signal right now.")
                            } else {
                                LazyColumn(
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    items(scannedStocks) { record ->
                                        ScannedStockCard(
                                            result = record,
                                            isNse = isNse,
                                            onClick = { activeDialogItem = record.item }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Overlay dialog sheet for card selections
        activeDialogItem?.let { item ->
            StockDetailDialog(
                item = item,
                isNse = isNse,
                onDismiss = { activeDialogItem = null },
                onAddAlert = { ticker, valBound, criteria ->
                    viewModel.addCustomAlert(ticker, valBound, criteria)
                    activeDialogItem = null
                }
            )
        }

        // Slide Drawer / Sheet Alert overlay
        if (isAlertDrawerOpen) {
            AlertCenterDrawer(
                alerts = alerts,
                customTriggers = customTriggers,
                onClose = { isAlertDrawerOpen = false },
                onDismissAlert = { id -> viewModel.markAlertAsRead(id) },
                onRemoveTrigger = { id -> viewModel.removeCustomAlert(id) },
                onClearAllAlerts = { viewModel.clearAllAlerts() }
            )
        }

        if (showSheetsConfig) {
            var nseUrlInput by remember { mutableStateOf(customNseId) }
            var nasdaqUrlInput by remember { mutableStateOf(customNasdaqId) }
            
            var isNseTesting by remember { mutableStateOf(false) }
            var isNasdaqTesting by remember { mutableStateOf(false) }
            
            var nseTestResult by remember { mutableStateOf<Boolean?>(null) }
            var nasdaqTestResult by remember { mutableStateOf<Boolean?>(null) }
            
            val coroutineScope = rememberCoroutineScope()

            AlertDialog(
                onDismissRequest = { showSheetsConfig = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Google Sheets Data Sources", style = MaterialTheme.typography.titleLarge)
                    }
                },
                text = {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                text = "Connect custom Google Sheets with live pricing tickers using GOOGLEFINANCE.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // NSE Source Section
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        "NSE (India) Spreadsheet Source",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = nseUrlInput,
                                        onValueChange = { 
                                            nseUrlInput = it
                                            nseTestResult = null 
                                        },
                                        label = { Text("Google Sheet URL or ID") },
                                        modifier = Modifier.fillMaxWidth(),
                                        maxLines = 2,
                                        placeholder = { Text("Paste URL or ID") }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Button(
                                            onClick = {
                                                coroutineScope.launch {
                                                    isNseTesting = true
                                                    nseTestResult = viewModel.testSheetConnection(nseUrlInput)
                                                    isNseTesting = false
                                                }
                                            },
                                            enabled = !isNseTesting,
                                            modifier = Modifier.height(36.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                                        ) {
                                            if (isNseTesting) {
                                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Testing...", style = MaterialTheme.typography.labelSmall)
                                            } else {
                                                Text("Test", style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                        
                                        nseTestResult?.let { success ->
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = if (success) Icons.Default.CheckCircle else Icons.Default.Warning,
                                                    contentDescription = null,
                                                    tint = if (success) Color(0xFF10B981) else Color(0xFFEF4444),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (success) "Connected" else "Failed/401",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (success) Color(0xFF10B981) else Color(0xFFEF4444)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // NASDAQ Source Section
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        "NASDAQ (US) Spreadsheet Source",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = nasdaqUrlInput,
                                        onValueChange = { 
                                            nasdaqUrlInput = it
                                            nasdaqTestResult = null 
                                        },
                                        label = { Text("Google Sheet URL or ID") },
                                        modifier = Modifier.fillMaxWidth(),
                                        maxLines = 2,
                                        placeholder = { Text("Paste URL or ID") }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Button(
                                            onClick = {
                                                coroutineScope.launch {
                                                    isNasdaqTesting = true
                                                    nasdaqTestResult = viewModel.testSheetConnection(nasdaqUrlInput)
                                                    isNasdaqTesting = false
                                                }
                                            },
                                            enabled = !isNasdaqTesting,
                                            modifier = Modifier.height(36.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                                        ) {
                                            if (isNasdaqTesting) {
                                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Testing...", style = MaterialTheme.typography.labelSmall)
                                            } else {
                                                Text("Test", style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                        
                                        nasdaqTestResult?.let { success ->
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = if (success) Icons.Default.CheckCircle else Icons.Default.Warning,
                                                    contentDescription = null,
                                                    tint = if (success) Color(0xFF10B981) else Color(0xFFEF4444),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (success) "Connected" else "Failed/401",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (success) Color(0xFF10B981) else Color(0xFFEF4444)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Setup Instructions Guide
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        "💡 Sharing & Setup Instructions",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "To avoid 401 Unauthorized errors:\n" +
                                        "1. Set access to \"Anyone with the link can view\".\n" +
                                        "2. Or go to File -> Share -> Publish to the web as CSV.\n" +
                                        "\nNote: Spreadsheet must contain matching tabs: \"Industry Analysis\", \"Liquid Momentum Leaders\", \"Momentum Top Names\", \"Doublers\".",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Reset Default Template Button
                        item {
                            TextButton(
                                onClick = {
                                    nseUrlInput = StockRepository.DEFAULT_NSE_ID
                                    nasdaqUrlInput = StockRepository.DEFAULT_NASDAQ_ID
                                    nseTestResult = null
                                    nasdaqTestResult = null
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Reset to Default Stock Tracker", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            sharedPrefs.edit()
                                .putString("nseId", nseUrlInput)
                                .putString("nasdaqId", nasdaqUrlInput)
                                .apply()
                            viewModel.updateSpreadsheetIds(nseUrlInput, nasdaqUrlInput)
                            showSheetsConfig = false
                        }
                    ) {
                        Text("Save & Sync")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSheetsConfig = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

// Visual layout placeholders
@Composable
fun EmptyStatePlaceholder(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                modifier = Modifier.size(60.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun IndustryAnalysisCard(
    item: IndustryPerformance,
    onClick: () -> Unit
) {
    val isPositive = item.performance1M >= 0.0
    val trendColor = if (isPositive) Color(0xFF10B981) else Color(0xFFEF4444)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.industryName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Status: ${item.status}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${if (item.performance1M >= 0) "+" else ""}${String.format("%.1f", item.performance1M)}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = trendColor
                    )
                    Text(
                        text = "1M performance",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Bulls / Bears Ratio indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Bulls: ${item.advancingStocks}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Bears: ${item.decliningStocks}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFEF4444),
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "Weekly Perf: ${if (item.performance1W >= 0) "+" else ""}${item.performance1W}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun LiquidLeaderCard(
    item: LiquidLeader,
    isNse: Boolean,
    onClick: () -> Unit
) {
    val isPositive = item.changePercent >= 0.0
    val trendColor = if (isPositive) Color(0xFF10B981) else Color(0xFFEF4444)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank badge
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.momentumRank.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Symbol and name
            Column(modifier = Modifier.weight(1.2f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.ticker,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "RS: ${item.relativeStrength}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Sparkline vector drawing
            Sparkline(
                modifier = Modifier
                    .weight(0.9f)
                    .padding(horizontal = 4.dp),
                changePercent = item.changePercent
            )

            // Price values
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End
            ) {
                val formattedPrice = if (isNse) "₹${String.format("%,.2f", item.lastPrice)}" else "\$${String.format("%,.2f", item.lastPrice)}"
                Text(
                    text = formattedPrice,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.End
                )
                Text(
                    text = "${if (item.changePercent >= 0) "+" else ""}${String.format("%.2f", item.changePercent)}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = trendColor,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Composable
fun MomentumTopNameCard(
    item: MomentumTopName,
    isNse: Boolean,
    onClick: () -> Unit
) {
    val isPositive = item.changePercent >= 0.0
    val trendColor = if (isPositive) Color(0xFF10B981) else Color(0xFFEF4444)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1.3f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.ticker,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = item.breakoutStatus,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Inline chart
                Sparkline(
                    modifier = Modifier
                        .weight(0.8f)
                        .padding(horizontal = 4.dp),
                    changePercent = item.changePercent
                )

                Column(
                    modifier = Modifier.weight(0.9f),
                    horizontalAlignment = Alignment.End
                ) {
                    val formattedPrice = if (isNse) "₹${String.format("%,.2f", item.lastPrice)}" else "\$${String.format("%,.2f", item.lastPrice)}"
                    Text(
                        text = formattedPrice,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${if (item.changePercent >= 0) "+" else ""}${String.format("%.2f", item.changePercent)}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = trendColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "1M: ${if (item.return1M >= 0) "+" else ""}${item.return1M}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (item.return1M >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                    )
                    Text(
                        text = "3M: ${if (item.return3M >= 0) "+" else ""}${item.return3M}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (item.return3M >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                    )
                }

                Text(
                    text = "Score: ${item.momentumScore}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
fun DoublerCard(
    item: DoublerItem,
    isNse: Boolean,
    onClick: () -> Unit
) {
    val isPositive = item.changePercent >= 0.0
    val trendColor = if (isPositive) Color(0xFF10B981) else Color(0xFFEF4444)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1.3f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.ticker,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(
                                    Color(0xFF818CF8).copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = item.marketCapType,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF818CF8)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Graphic
                Sparkline(
                    modifier = Modifier
                        .weight(0.8f)
                        .padding(horizontal = 4.dp),
                    changePercent = item.changePercent
                )

                Column(
                    modifier = Modifier.weight(0.9f),
                    horizontalAlignment = Alignment.End
                ) {
                    val formattedPrice = if (isNse) "₹${String.format("%,.2f", item.lastPrice)}" else "\$${String.format("%,.2f", item.lastPrice)}"
                    Text(
                        text = formattedPrice,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${if (item.changePercent >= 0) "+" else ""}${String.format("%.2f", item.changePercent)}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = trendColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "Valuation: ${if (item.peRatio <= 0) "--" else "${item.peRatio}x PE"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Growth: +${item.growthRate}% yr",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .background(
                            Color(0x2210B981),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "+${item.upsidePercent.toInt()}% potential",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                }
            }
        }
    }
}

@Composable
fun ScannedStockCard(
    result: ScannedStockResult,
    isNse: Boolean,
    onClick: () -> Unit
) {
    val item = result.item
    val isPositive = item.changePercent >= 0.0
    val trendColor = if (isPositive) Color(0xFF10B981) else Color(0xFFEF4444)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1.5f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.ticker,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            val category = when (item) {
                                is LiquidLeader -> "Liquid Leader"
                                is MomentumTopName -> "Top Momentum"
                                is DoublerItem -> "Doubler Spot"
                                else -> "Standard"
                            }
                            Text(
                                text = category,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    val formattedPrice = if (isNse) "₹${String.format("%,.2f", item.lastPrice)}" else "\$${String.format("%,.2f", item.lastPrice)}"
                    Text(
                        text = formattedPrice,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${if (item.changePercent >= 0) "+" else ""}${String.format("%.2f", item.changePercent)}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = trendColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(10.dp))

            // Show crossing confirmation badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1.2f)) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Color(0xFF10B981), shape = CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Trigger Active",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                }

                val matchedDesc = when (result.criteria) {
                    TechnicalAlertCriteria.CROSS_UP_EMA10 -> "UP 10 EMA (${String.format("%.2f", result.techState.ema10Current)})"
                    TechnicalAlertCriteria.CROSS_UP_EMA20 -> "UP 20 EMA (${String.format("%.2f", result.techState.ema20Current)})"
                    TechnicalAlertCriteria.CROSS_DOWN_EMA10 -> "DOWN 10 EMA (${String.format("%.2f", result.techState.ema10Current)})"
                    TechnicalAlertCriteria.CROSS_DOWN_EMA20 -> "DOWN 20 EMA (${String.format("%.2f", result.techState.ema20Current)})"
                    TechnicalAlertCriteria.CROSS_PREV_DAY_HIGH -> "Prev Day High (${String.format("%.2f", result.techState.prevDayHigh)})"
                    TechnicalAlertCriteria.CROSS_PREV_WEEK_HIGH -> "Prev Week High (${String.format("%.2f", result.techState.prevWeekHigh)})"
                    TechnicalAlertCriteria.CROSS_PREV_MONTH_HIGH -> "Prev Month High (${String.format("%.2f", result.techState.prevMonthHigh)})"
                    else -> "Indicator Match"
                }

                Text(
                    text = matchedDesc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1.8f),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}
