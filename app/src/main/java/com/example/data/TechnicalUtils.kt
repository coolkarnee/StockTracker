package com.example.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Random

data class Candle(
    val index: Int,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double,
    val label: String
)

data class StockTechnicalState(
    val currentPrice: Double,
    val prevPrice: Double,
    val ema10Current: Double,
    val ema10Prev: Double,
    val ema20Current: Double,
    val ema20Prev: Double,
    val ema50Current: Double,
    val ema50Prev: Double,
    val prevDayHigh: Double,
    val prevWeekHigh: Double,
    val prevMonthHigh: Double
) {
    fun isCrossingUp10Ema(): Boolean {
        return prevPrice < ema10Prev && currentPrice >= ema10Current
    }

    fun isCrossingUp20Ema(): Boolean {
        return prevPrice < ema20Prev && currentPrice >= ema20Current
    }

    fun isCrossingDown10Ema(): Boolean {
        return prevPrice > ema10Prev && currentPrice <= ema10Current
    }

    fun isCrossingDown20Ema(): Boolean {
        return prevPrice > ema20Prev && currentPrice <= ema20Current
    }

    fun isCrossingPrevDayHigh(): Boolean {
        return prevPrice < prevDayHigh && currentPrice >= prevDayHigh
    }

    fun isCrossingPrevWeekHigh(): Boolean {
        return prevPrice < prevWeekHigh && currentPrice >= prevWeekHigh
    }

    fun isCrossingPrevMonthHigh(): Boolean {
        return prevPrice < prevMonthHigh && currentPrice >= prevMonthHigh
    }
}

object TechnicalUtils {

    // Exponential Moving Averages (EMAs) math
    fun calculateEMA(data: List<Double>, period: Int): List<Double?> {
        if (data.size < period) {
            return List(data.size) { null }
        }
        val emaList = ArrayList<Double?>()
        val multiplier = 2.0 / (period + 1)
        
        for (i in 0 until (period - 1)) {
            emaList.add(null)
        }
        
        val initialSma = data.subList(0, period).average()
        emaList.add(initialSma)
        
        var prevEma = initialSma
        for (i in period until data.size) {
            val currentEma = (data[i] - prevEma) * multiplier + prevEma
            emaList.add(currentEma)
            prevEma = currentEma
        }
        return emaList
    }

    // Generate simulated candle steps walks based on lastPrice
    fun generateCandleData(
        lastPrice: Double,
        changePercent: Double,
        count: Int,
        volatility: Double
    ): List<Candle> {
        val seed = (lastPrice.toLong() * 123 + count * 5)
        val random = Random(seed)
        val rawCandles = ArrayList<Candle>()
        
        var currentPrice = lastPrice
        val baseVolume = 350000.0

        for (i in 0 until count) {
            val dailyReturn = (random.nextGaussian() * volatility)
            val close = currentPrice
            val open = currentPrice / (1.0 + dailyReturn)
            
            val high = maxOf(open, close) * (1.0 + random.nextDouble() * volatility * 0.4)
            val low = minOf(open, close) * (1.0 - random.nextDouble() * volatility * 0.4)
            val dailyVolume = baseVolume * (0.4 + random.nextDouble() * 1.5)
            
            rawCandles.add(
                Candle(
                    index = count - 1 - i,
                    open = open,
                    high = high,
                    low = low,
                    close = close,
                    volume = dailyVolume,
                    label = getSimulatedDateLabel(i)
                )
            )
            currentPrice = open
        }
        return rawCandles.reversed()
    }

    private fun getSimulatedDateLabel(daysAgo: Int): String {
        val baseTime = System.currentTimeMillis() - daysAgo * 24L * 60 * 60 * 1000
        val sdf = SimpleDateFormat("dd MMM", Locale.US)
        return sdf.format(Date(baseTime))
    }

    // Compute technical state for alerts and scanners
    fun computeTechnicalState(lastPrice: Double, changePercent: Double): StockTechnicalState {
        val candles = generateCandleData(lastPrice, changePercent, 80, 0.021)
        
        val closes = candles.map { it.close }
        val ema10 = calculateEMA(closes, 10)
        val ema20 = calculateEMA(closes, 20)
        val ema50 = calculateEMA(closes, 50)

        val idxCurr = closes.lastIndex
        val idxPrev = closes.lastIndex - 1

        val currentPrice = closes[idxCurr]
        val prevPrice = closes[idxPrev]

        val ema10Curr = ema10[idxCurr] ?: currentPrice
        val ema10Prev = ema10[idxPrev] ?: prevPrice

        val ema20Curr = ema20[idxCurr] ?: currentPrice
        val ema20Prev = ema20[idxPrev] ?: prevPrice

        val ema50Curr = ema50[idxCurr] ?: currentPrice
        val ema50Prev = ema50[idxPrev] ?: prevPrice

        // Previous Day High (high of candles[lastIndex - 1])
        val prevDayHigh = if (candles.size > 1) candles[candles.lastIndex - 1].high else currentPrice

        // Previous Week High (highest of days 2 to 6, excluding today which is element 0 from end/lastIndex)
        val prevWeekHigh = if (candles.size >= 7) {
            candles.subList(candles.size - 7, candles.size - 1).maxOf { it.high }
        } else {
            currentPrice * 1.02
        }

        // Previous Month High (highest of past 20 candles/trading days, excluding current)
        val prevMonthHigh = if (candles.size >= 21) {
            candles.subList(candles.size - 21, candles.size - 1).maxOf { it.high }
        } else {
            currentPrice * 1.05
        }

        return StockTechnicalState(
            currentPrice = currentPrice,
            prevPrice = prevPrice,
            ema10Current = ema10Curr,
            ema10Prev = ema10Prev,
            ema20Current = ema20Curr,
            ema20Prev = ema20Prev,
            ema50Current = ema50Curr,
            ema50Prev = ema50Prev,
            prevDayHigh = prevDayHigh,
            prevWeekHigh = prevWeekHigh,
            prevMonthHigh = prevMonthHigh
        )
    }
}
