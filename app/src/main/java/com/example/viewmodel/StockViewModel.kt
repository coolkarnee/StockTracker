package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class SortOption {
    DEFAULT,
    PRICE_ASC, PRICE_DESC,
    CHANGE_ASC, CHANGE_DESC,
    SCORE_DESC, VOLUME_DESC
}

enum class SyncState {
    IDLE, SYNCING, LIVE, OFFLINE_CACHE
}

enum class TechnicalAlertCriteria {
    PRICE_ABOVE,
    PRICE_BELOW,
    CROSS_UP_EMA10,
    CROSS_UP_EMA20,
    CROSS_DOWN_EMA10,
    CROSS_DOWN_EMA20,
    CROSS_PREV_DAY_HIGH,
    CROSS_PREV_WEEK_HIGH,
    CROSS_PREV_MONTH_HIGH
}

data class UserCustomAlert(
    val id: String,
    val ticker: String,
    val targetPrice: Double,
    val exchangeIsNse: Boolean,
    val criteria: TechnicalAlertCriteria,
    val isTriggered: Boolean = false,
    val triggerTime: Long = 0L
)

data class ScannedStockResult(
    val item: StockItem,
    val criteria: TechnicalAlertCriteria,
    val techState: StockTechnicalState
)

class StockViewModel : ViewModel() {

    private val repository = StockRepository()

    // Custom Spreadsheet IDs (StateFlows initialized to defaults)
    private val _customNseId = MutableStateFlow(StockRepository.DEFAULT_NSE_ID)
    val customNseId = _customNseId.asStateFlow()

    private val _customNasdaqId = MutableStateFlow(StockRepository.DEFAULT_NASDAQ_ID)
    val customNasdaqId = _customNasdaqId.asStateFlow()

    fun updateSpreadsheetIds(nse: String, nasdaq: String) {
        val cleanedNse = StockRepository.extractId(nse)
        val cleanedNasdaq = StockRepository.extractId(nasdaq)
        _customNseId.value = cleanedNse
        _customNasdaqId.value = cleanedNasdaq
        repository.nseId = cleanedNse
        repository.nasdaqId = cleanedNasdaq
        refreshData()
    }

    suspend fun testSheetConnection(sheetInput: String): Boolean {
        return repository.testSheetConnection(sheetInput)
    }

    // Active Exchange (True = NSE, False = Nasdaq)
    private val _isNse = MutableStateFlow(true)
    val isNse = _isNse.asStateFlow()

    // Sub-Tab Index (0 = Industry, 1 = Liquid Leaders, 2 = Momentum Top, 3 = Doublers)
    private val _currentSubTab = MutableStateFlow(0)
    val currentSubTab = _currentSubTab.asStateFlow()

    // Network & Sync State
    private val _syncState = MutableStateFlow(SyncState.IDLE)
    val syncState = _syncState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    // Filter, Search, and Sort state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _sortBy = MutableStateFlow(SortOption.DEFAULT)
    val sortBy = _sortBy.asStateFlow()

    // Raw datasets
    private val _industries = MutableStateFlow<List<IndustryPerformance>>(emptyList())
    private val _liquidLeaders = MutableStateFlow<List<LiquidLeader>>(emptyList())
    private val _momentumTopNames = MutableStateFlow<List<MomentumTopName>>(emptyList())
    private val _doublers = MutableStateFlow<List<DoublerItem>>(emptyList())

    // Filtered lists
    val filteredIndustries = combine(_industries, _searchQuery, _sortBy) { list, query, sort ->
        var res = list
        if (query.isNotBlank()) {
            res = res.filter { it.industryName.lowercase().contains(query.lowercase()) }
        }
        when (sort) {
            SortOption.CHANGE_ASC -> res.sortedBy { it.performance1M }
            SortOption.CHANGE_DESC -> res.sortedByDescending { it.performance1M }
            SortOption.PRICE_ASC -> res.sortedBy { it.performance1W }
            SortOption.PRICE_DESC -> res.sortedByDescending { it.performance1W }
            SortOption.SCORE_DESC -> res.sortedByDescending { it.score }
            else -> res
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredLiquidLeaders = combine(_liquidLeaders, _searchQuery, _sortBy) { list, query, sort ->
        var res = list
        if (query.isNotBlank()) {
            res = res.filter {
                it.ticker.lowercase().contains(query.lowercase()) ||
                        it.name.lowercase().contains(query.lowercase())
            }
        }
        when (sort) {
            SortOption.PRICE_ASC -> res.sortedBy { it.lastPrice }
            SortOption.PRICE_DESC -> res.sortedByDescending { it.lastPrice }
            SortOption.CHANGE_ASC -> res.sortedBy { it.changePercent }
            SortOption.CHANGE_DESC -> res.sortedByDescending { it.changePercent }
            SortOption.SCORE_DESC -> res.sortedByDescending { it.relativeStrength }
            SortOption.VOLUME_DESC -> res.sortedByDescending { parseVolumeNum(it.volume) }
            else -> res.sortedBy { it.momentumRank }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredMomentumTopNames = combine(_momentumTopNames, _searchQuery, _sortBy) { list, query, sort ->
        var res = list
        if (query.isNotBlank()) {
            res = res.filter {
                it.ticker.lowercase().contains(query.lowercase()) ||
                        it.name.lowercase().contains(query.lowercase())
            }
        }
        when (sort) {
            SortOption.PRICE_ASC -> res.sortedBy { it.lastPrice }
            SortOption.PRICE_DESC -> res.sortedByDescending { it.lastPrice }
            SortOption.CHANGE_ASC -> res.sortedBy { it.changePercent }
            SortOption.CHANGE_DESC -> res.sortedByDescending { it.changePercent }
            SortOption.SCORE_DESC -> res.sortedByDescending { it.momentumScore }
            SortOption.VOLUME_DESC -> res.sortedByDescending { parseVolumeNum(it.volume) }
            else -> res
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredDoublers = combine(_doublers, _searchQuery, _sortBy) { list, query, sort ->
        var res = list
        if (query.isNotBlank()) {
            res = res.filter {
                it.ticker.lowercase().contains(query.lowercase()) ||
                        it.name.lowercase().contains(query.lowercase())
            }
        }
        when (sort) {
            SortOption.PRICE_ASC -> res.sortedBy { it.lastPrice }
            SortOption.PRICE_DESC -> res.sortedByDescending { it.lastPrice }
            SortOption.CHANGE_ASC -> res.sortedBy { it.changePercent }
            SortOption.CHANGE_DESC -> res.sortedByDescending { it.changePercent }
            SortOption.SCORE_DESC -> res.sortedByDescending { it.upsidePercent }
            SortOption.VOLUME_DESC -> res.sortedByDescending { parseVolumeNum(it.volume) }
            else -> res
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Alerts State
    private val _alerts = MutableStateFlow<List<AlertSignal>>(FallbackData.fallbackAlerts)
    
    // Alerts should only be shown for the stocks from only for 3 tabs - Liquid Momentum Leaders, Momentum Top Names and Doublers for both NSE and NASDAQ
    val alerts = combine(_alerts, _liquidLeaders, _momentumTopNames, _doublers) { list, leaders, momentum, doublers ->
        val allowedTickers = (leaders.map { it.ticker.uppercase() } + 
                              momentum.map { it.ticker.uppercase() } + 
                              doublers.map { it.ticker.uppercase() }).toSet()
        list.filter { 
            it.ticker.uppercase() in allowedTickers
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Custom alerts list
    private val _customAlertTriggers = MutableStateFlow<List<UserCustomAlert>>(emptyList())
    val customAlertTriggers = _customAlertTriggers.asStateFlow()

    // Selected Scan criteria
    private val _selectedScanCriteria = MutableStateFlow<TechnicalAlertCriteria?>(TechnicalAlertCriteria.CROSS_UP_EMA10)
    val selectedScanCriteria = _selectedScanCriteria.asStateFlow()

    fun setScanCriteria(criteria: TechnicalAlertCriteria?) {
        _selectedScanCriteria.value = criteria
    }

    val scannedStocks = combine(
        _selectedScanCriteria,
        _liquidLeaders,
        _momentumTopNames,
        _doublers
    ) { criteria, leaders, momentum, doublers ->
        if (criteria == null) return@combine emptyList<ScannedStockResult>()
        
        val allItems = (leaders + momentum + doublers).distinctBy { it.ticker }
        
        allItems.mapNotNull { item ->
            val techState = TechnicalUtils.computeTechnicalState(item.lastPrice, item.changePercent)
            val matches = when (criteria) {
                TechnicalAlertCriteria.PRICE_ABOVE -> false
                TechnicalAlertCriteria.PRICE_BELOW -> false
                TechnicalAlertCriteria.CROSS_UP_EMA10 -> techState.isCrossingUp10Ema()
                TechnicalAlertCriteria.CROSS_UP_EMA20 -> techState.isCrossingUp20Ema()
                TechnicalAlertCriteria.CROSS_DOWN_EMA10 -> techState.isCrossingDown10Ema()
                TechnicalAlertCriteria.CROSS_DOWN_EMA20 -> techState.isCrossingDown20Ema()
                TechnicalAlertCriteria.CROSS_PREV_DAY_HIGH -> techState.isCrossingPrevDayHigh()
                TechnicalAlertCriteria.CROSS_PREV_WEEK_HIGH -> techState.isCrossingPrevWeekHigh()
                TechnicalAlertCriteria.CROSS_PREV_MONTH_HIGH -> techState.isCrossingPrevMonthHigh()
            }
            if (matches) {
                ScannedStockResult(
                    item = item,
                    criteria = criteria,
                    techState = techState
                )
            } else {
                null
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Load initial data
        refreshData()
    }

    fun setExchange(isNse: Boolean) {
        if (_isNse.value != isNse) {
            _isNse.value = isNse
            refreshData()
        }
    }

    fun setSubTab(index: Int) {
        _currentSubTab.value = index
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortBy(option: SortOption) {
        _sortBy.value = option
    }

    fun refreshData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _syncState.value = SyncState.SYNCING

            val activeExchange = _isNse.value

            // Attempt network connectivity status check
            val isOnline = repository.checkNetworkStatus(activeExchange)

            // Let's load the sheets concurrently or sequentially
            try {
                val indList = repository.getIndustryAnalysis(activeExchange)
                val liqList = repository.getLiquidLeaders(activeExchange)
                val momList = repository.getMomentumTopNames(activeExchange)
                val dblList = repository.getDoublers(activeExchange)

                _industries.value = indList
                _liquidLeaders.value = liqList
                _momentumTopNames.value = momList
                _doublers.value = dblList

                _syncState.value = if (isOnline) SyncState.LIVE else SyncState.OFFLINE_CACHE
            } catch (e: Exception) {
                // Fall back completely
                _industries.value = if (activeExchange) FallbackData.nseIndustries else FallbackData.nasdaqIndustries
                _liquidLeaders.value = if (activeExchange) FallbackData.nseLiquidLeaders else FallbackData.nasdaqLiquidLeaders
                _momentumTopNames.value = if (activeExchange) FallbackData.nseMomentumTopNames else FallbackData.nasdaqMomentumTopNames
                _doublers.value = if (activeExchange) FallbackData.nseDoublers else FallbackData.nasdaqDoublers

                _syncState.value = SyncState.OFFLINE_CACHE
            }

            _isRefreshing.value = false

            // Re-evaluate custom price alerts against loaded values
            evaluateCustomAlerts()
        }
    }

    // Add real-time user-defined price alert
    fun addCustomAlert(ticker: String, price: Double, isAbove: Boolean) {
        val crit = if (isAbove) TechnicalAlertCriteria.PRICE_ABOVE else TechnicalAlertCriteria.PRICE_BELOW
        addCustomAlert(ticker, price, crit)
    }

    fun addCustomAlert(ticker: String, price: Double, criteria: TechnicalAlertCriteria) {
        val activeNse = _isNse.value
        val alertId = "custom_${System.currentTimeMillis()}"
        val newAlert = UserCustomAlert(
            id = alertId,
            ticker = ticker.uppercase().trim(),
            targetPrice = price,
            exchangeIsNse = activeNse,
            criteria = criteria
        )
        _customAlertTriggers.value = _customAlertTriggers.value + newAlert

        // Send confirmation alert
        val confirmSignal = AlertSignal(
            id = "sys_${System.currentTimeMillis()}",
            timestamp = System.currentTimeMillis(),
            ticker = ticker.uppercase().trim(),
            title = "Alert Setup Successful",
            description = "Active for ${ticker.uppercase()}: ${getCriteriaLabel(criteria, price)}",
            level = AlertLevel.INFO
        )
        _alerts.value = listOf(confirmSignal) + _alerts.value

        evaluateCustomAlerts()
    }

    fun removeCustomAlert(id: String) {
        _customAlertTriggers.value = _customAlertTriggers.value.filter { it.id != id }
    }

    fun getCriteriaLabel(criteria: TechnicalAlertCriteria, price: Double = 0.0): String {
        return when (criteria) {
            TechnicalAlertCriteria.PRICE_ABOVE -> "Price Raw Cross Above $price"
            TechnicalAlertCriteria.PRICE_BELOW -> "Price Raw Cross Below $price"
            TechnicalAlertCriteria.CROSS_UP_EMA10 -> "Crossing UP 10 EMA"
            TechnicalAlertCriteria.CROSS_UP_EMA20 -> "Crossing UP 20 EMA"
            TechnicalAlertCriteria.CROSS_DOWN_EMA10 -> "Crossing DOWN 10 EMA"
            TechnicalAlertCriteria.CROSS_DOWN_EMA20 -> "Crossing DOWN 20 EMA"
            TechnicalAlertCriteria.CROSS_PREV_DAY_HIGH -> "Crossing Previous Day High"
            TechnicalAlertCriteria.CROSS_PREV_WEEK_HIGH -> "Crossing Previous Week High"
            TechnicalAlertCriteria.CROSS_PREV_MONTH_HIGH -> "Crossing Previous Month High"
        }
    }

    private fun findTickerChangePercent(ticker: String): Double {
        val inLeaders = _liquidLeaders.value.firstOrNull { it.ticker.equals(ticker, ignoreCase = true) }?.changePercent
        if (inLeaders != null) return inLeaders
        val inMomentum = _momentumTopNames.value.firstOrNull { it.ticker.equals(ticker, ignoreCase = true) }?.changePercent
        if (inMomentum != null) return inMomentum
        val inDoublers = _doublers.value.firstOrNull { it.ticker.equals(ticker, ignoreCase = true) }?.changePercent
        return inDoublers ?: 0.0
    }

    private fun getStockTechnicalState(ticker: String, price: Double): StockTechnicalState {
        val cp = findTickerChangePercent(ticker)
        return TechnicalUtils.computeTechnicalState(price, cp)
    }

    private fun evaluateCustomAlerts() {
        val currentTriggers = _customAlertTriggers.value
        if (currentTriggers.isEmpty()) return

        val activeExchange = _isNse.value
        val updatedTriggers = currentTriggers.map { alert ->
            if (alert.exchangeIsNse == activeExchange && !alert.isTriggered) {
                // Search for ticker price to check if met
                val price = findTickerPrice(alert.ticker)
                if (price != null) {
                    val techState = getStockTechnicalState(alert.ticker, price)
                    val met = when (alert.criteria) {
                        TechnicalAlertCriteria.PRICE_ABOVE -> price >= alert.targetPrice
                        TechnicalAlertCriteria.PRICE_BELOW -> price <= alert.targetPrice
                        TechnicalAlertCriteria.CROSS_UP_EMA10 -> techState.isCrossingUp10Ema()
                        TechnicalAlertCriteria.CROSS_UP_EMA20 -> techState.isCrossingUp20Ema()
                        TechnicalAlertCriteria.CROSS_DOWN_EMA10 -> techState.isCrossingDown10Ema()
                        TechnicalAlertCriteria.CROSS_DOWN_EMA20 -> techState.isCrossingDown20Ema()
                        TechnicalAlertCriteria.CROSS_PREV_DAY_HIGH -> techState.isCrossingPrevDayHigh()
                        TechnicalAlertCriteria.CROSS_PREV_WEEK_HIGH -> techState.isCrossingPrevWeekHigh()
                        TechnicalAlertCriteria.CROSS_PREV_MONTH_HIGH -> techState.isCrossingPrevMonthHigh()
                    }
                    if (met) {
                        triggerVisualAlert(alert, price)
                        alert.copy(isTriggered = true, triggerTime = System.currentTimeMillis())
                    } else alert
                } else alert
            } else alert
        }
        _customAlertTriggers.value = updatedTriggers
    }

    private fun triggerVisualAlert(alert: UserCustomAlert, currentPrice: Double) {
        val cond = getCriteriaLabel(alert.criteria, alert.targetPrice)
        val signal = AlertSignal(
            id = "trig_${System.currentTimeMillis()}",
            timestamp = System.currentTimeMillis(),
            ticker = alert.ticker,
            title = "🎯 Actionable Alert Met!",
            description = "${alert.ticker} has met the alert condition: $cond! (Current Price: $currentPrice)",
            level = AlertLevel.URGENT
        )
        _alerts.value = listOf(signal) + _alerts.value
    }

    private fun findTickerPrice(ticker: String): Double? {
        val inLeaders = _liquidLeaders.value.firstOrNull { it.ticker.equals(ticker, ignoreCase = true) }?.lastPrice
        if (inLeaders != null) return inLeaders
        val inMomentum = _momentumTopNames.value.firstOrNull { it.ticker.equals(ticker, ignoreCase = true) }?.lastPrice
        if (inMomentum != null) return inMomentum
        val inDoublers = _doublers.value.firstOrNull { it.ticker.equals(ticker, ignoreCase = true) }?.lastPrice
        return inDoublers
    }

    fun markAlertAsRead(id: String) {
        _alerts.value = _alerts.value.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
    }

    fun clearAllAlerts() {
        _alerts.value = emptyList()
    }

    private fun parseVolumeNum(volStr: String): Double {
        val clean = volStr.uppercase().replace("M", "").replace("K", "").replace(",", "").trim()
        val num = clean.toDoubleOrNull() ?: 0.0
        return if (volStr.uppercase().contains("M")) num * 1_000_000 else if (volStr.uppercase().contains("K")) num * 1_000 else num
    }
}
