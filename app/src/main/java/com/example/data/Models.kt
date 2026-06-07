package com.example.data

sealed interface StockItem {
    val ticker: String
    val name: String
    val lastPrice: Double
    val changePercent: Double
    val volume: String
}

data class IndustryPerformance(
    val industryName: String,
    val performance1W: Double,
    val performance1M: Double,
    val performance3M: Double,
    val advancingStocks: Int,
    val decliningStocks: Int,
    val status: String,
    val score: Double = 0.0
)

data class LiquidLeader(
    override val ticker: String,
    override val name: String,
    override val lastPrice: Double,
    override val changePercent: Double,
    override val volume: String,
    val momentumRank: Int,
    val relativeStrength: Int,
    val dist52wHigh: Double
) : StockItem

data class MomentumTopName(
    override val ticker: String,
    override val name: String,
    override val lastPrice: Double,
    override val changePercent: Double,
    override val volume: String,
    val return1M: Double,
    val return3M: Double,
    val breakoutStatus: String,
    val momentumScore: Double
) : StockItem

data class DoublerItem(
    override val ticker: String,
    override val name: String,
    override val lastPrice: Double,
    override val changePercent: Double,
    override val volume: String,
    val low52W: Double,
    val high52W: Double,
    val upsidePercent: Double,
    val peRatio: Double,
    val marketCapType: String,
    val growthRate: Double
) : StockItem

data class AlertSignal(
    val id: String,
    val timestamp: Long,
    val ticker: String,
    val title: String,
    val description: String,
    val level: AlertLevel,
    val isRead: Boolean = false
)

enum class AlertLevel {
    INFO, MOMENTUM, BREAKOUT, URGENT
}
