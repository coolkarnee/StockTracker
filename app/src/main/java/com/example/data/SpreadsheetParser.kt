package com.example.data

import android.util.Log

object SpreadsheetParser {

    private const val TAG = "SpreadsheetParser"

    // Custom robust CSV tokenizer
    fun parseCsv(csvText: String): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        val lines = csvText.split(Regex("\\R")) // Handles both \n and \r\n
        for (line in lines) {
            if (line.isBlank() || line.startsWith("#")) continue
            val row = mutableListOf<String>()
            var current = StringBuilder()
            var inQuotes = false
            var i = 0
            while (i < line.length) {
                val ch = line[i]
                if (ch == '\"') {
                    inQuotes = !inQuotes
                } else if (ch == ',' && !inQuotes) {
                    row.add(current.toString().trim().removeSurrounding("\""))
                    current = StringBuilder()
                } else {
                    current.append(ch)
                }
                i++
            }
            row.add(current.toString().trim().removeSurrounding("\""))
            rows.add(row)
        }
        return rows
    }

    private fun cleanDouble(str: String): Double {
        if (str.isBlank()) return 0.0
        val sanitized = str.replace("%", "")
            .replace(",", "")
            .replace("$", "")
            .replace("₹", "")
            .replace("INR", "")
            .replace("+", "")
            .trim()
        return try {
            sanitized.toDouble()
        } catch (e: NumberFormatException) {
            0.0
        }
    }

    private fun cleanInt(str: String): Int {
        if (str.isBlank()) return 0
        val sanitized = str.replace(",", "").replace("+", "").trim()
        return try {
            sanitized.toInt()
        } catch (e: NumberFormatException) {
            0
        }
    }

    fun parseIndustryAnalysis(csvText: String): List<IndustryPerformance>? {
        val rows = parseCsv(csvText)
        if (rows.isEmpty()) return null
        
        val header = rows[0].map { it.lowercase() }
        val nameIdx = header.indexOfFirst { it.contains("industry") || it.contains("sector") || it.contains("name") }
        val w1Idx = header.indexOfFirst { it.contains("1w") || it.contains("weekly") || it.contains("week") }
        val m1Idx = header.indexOfFirst { it.contains("1m") || it.contains("monthly") || it.contains("month") }
        val m3Idx = header.indexOfFirst { it.contains("3m") || it.contains("quarter") }
        val advIdx = header.indexOfFirst { it.contains("adv") || it.contains("up") || it.contains("gain") }
        val decIdx = header.indexOfFirst { it.contains("dec") || it.contains("down") || it.contains("loss") }
        val statusIdx = header.indexOfFirst { it.contains("status") || it.contains("trend") || it.contains("rating") }
        val scoreIdx = header.indexOfFirst { it.contains("score") || it.contains("index") }

        if (nameIdx == -1) return null

        val list = mutableListOf<IndustryPerformance>()
        for (i in 1 until rows.size) {
            val row = rows[i]
            if (row.size <= nameIdx || row[nameIdx].isBlank()) continue
            val keyName = row[nameIdx]
            val w1 = if (w1Idx != -1 && row.size > w1Idx) cleanDouble(row[w1Idx]) else 0.0
            val m1 = if (m1Idx != -1 && row.size > m1Idx) cleanDouble(row[m1Idx]) else 0.0
            val m3 = if (m3Idx != -1 && row.size > m3Idx) cleanDouble(row[m3Idx]) else 0.0
            val adv = if (advIdx != -1 && row.size > advIdx) cleanInt(row[advIdx]) else 0
            val dec = if (decIdx != -1 && row.size > decIdx) cleanInt(row[decIdx]) else 0
            val status = if (statusIdx != -1 && row.size > statusIdx) row[statusIdx] else "Average"
            val score = if (scoreIdx != -1 && row.size > scoreIdx) cleanDouble(row[scoreIdx]) else 0.0

            list.add(IndustryPerformance(keyName, w1, m1, m3, adv, dec, status, score))
        }
        return list
    }

    fun parseLiquidLeaders(csvText: String): List<LiquidLeader>? {
        val rows = parseCsv(csvText)
        if (rows.isEmpty()) return null

        val header = rows[0].map { it.lowercase() }
        val tickerIdx = header.indexOfFirst { it.contains("ticker") || it.contains("symbol") || it.contains("code") }
        val nameIdx = header.indexOfFirst { it.contains("name") || it.contains("company") || it.contains("desc") }
        val priceIdx = header.indexOfFirst { it.contains("price") || it.contains("ltp") || it.contains("cmp") || it.contains("close") }
        val changeIdx = header.indexOfFirst { it.contains("change") || it.contains("chg") || it.contains("%") }
        val volIdx = header.indexOfFirst { it.contains("volume") || it.contains("vol") || it.contains("qty") }
        val rankIdx = header.indexOfFirst { it.contains("rank") || it.contains("pos") }
        val rsIdx = header.indexOfFirst { it.contains("rs") || it.contains("strength") || it.contains("rating") }
        val distIdx = header.indexOfFirst { it.contains("52w") || it.contains("high") || it.contains("distance") }

        if (tickerIdx == -1) return null

        val list = mutableListOf<LiquidLeader>()
        for (i in 1 until rows.size) {
            val row = rows[i]
            if (row.size <= tickerIdx || row[tickerIdx].isBlank()) continue
            val ticker = row[tickerIdx]
            val name = if (nameIdx != -1 && row.size > nameIdx) row[nameIdx] else ticker
            val price = if (priceIdx != -1 && row.size > priceIdx) cleanDouble(row[priceIdx]) else 0.0
            val chg = if (changeIdx != -1 && row.size > changeIdx) cleanDouble(row[changeIdx]) else 0.0
            val vol = if (volIdx != -1 && row.size > volIdx) row[volIdx] else "N/A"
            val rank = if (rankIdx != -1 && row.size > rankIdx) cleanInt(row[rankIdx]) else i
            val rs = if (rsIdx != -1 && row.size > rsIdx) cleanInt(row[rsIdx]) else 80
            val dist = if (distIdx != -1 && row.size > distIdx) cleanDouble(row[distIdx]) else 2.5

            list.add(LiquidLeader(ticker, name, price, chg, vol, rank, rs, dist))
        }
        return list
    }

    fun parseMomentumTopNames(csvText: String): List<MomentumTopName>? {
        val rows = parseCsv(csvText)
        if (rows.isEmpty()) return null

        val header = rows[0].map { it.lowercase() }
        val tickerIdx = header.indexOfFirst { it.contains("ticker") || it.contains("symbol") || it.contains("code") }
        val nameIdx = header.indexOfFirst { it.contains("name") || it.contains("company") || it.contains("desc") }
        val priceIdx = header.indexOfFirst { it.contains("price") || it.contains("ltp") || it.contains("cmp") || it.contains("close") }
        val changeIdx = header.indexOfFirst { it.contains("change") || it.contains("chg") || it.contains("%") }
        val volIdx = header.indexOfFirst { it.contains("volume") || it.contains("vol") || it.contains("qty") }
        val m1Idx = header.indexOfFirst { it.contains("1m") || it.contains("monthly") }
        val m3Idx = header.indexOfFirst { it.contains("3m") || it.contains("quarter") }
        val breakoutIdx = header.indexOfFirst { it.contains("breakout") || it.contains("status") || it.contains("pivot") }
        val scoreIdx = header.indexOfFirst { it.contains("score") || it.contains("rank") || it.contains("rating") }

        if (tickerIdx == -1) return null

        val list = mutableListOf<MomentumTopName>()
        for (i in 1 until rows.size) {
            val row = rows[i]
            if (row.size <= tickerIdx || row[tickerIdx].isBlank()) continue
            val ticker = row[tickerIdx]
            val name = if (nameIdx != -1 && row.size > nameIdx) row[nameIdx] else ticker
            val price = if (priceIdx != -1 && row.size > priceIdx) cleanDouble(row[priceIdx]) else 0.0
            val chg = if (changeIdx != -1 && row.size > changeIdx) cleanDouble(row[changeIdx]) else 0.0
            val vol = if (volIdx != -1 && row.size > volIdx) row[volIdx] else "N/A"
            val m1 = if (m1Idx != -1 && row.size > m1Idx) cleanDouble(row[m1Idx]) else 0.0
            val m3 = if (m3Idx != -1 && row.size > m3Idx) cleanDouble(row[m3Idx]) else 0.0
            val breakout = if (breakoutIdx != -1 && row.size > breakoutIdx) row[breakoutIdx] else "Strong Momentum"
            val score = if (scoreIdx != -1 && row.size > scoreIdx) cleanDouble(row[scoreIdx]) else 85.0

            list.add(MomentumTopName(ticker, name, price, chg, vol, m1, m3, breakout, score))
        }
        return list
    }

    fun parseDoublers(csvText: String): List<DoublerItem>? {
        val rows = parseCsv(csvText)
        if (rows.isEmpty()) return null

        val header = rows[0].map { it.lowercase() }
        val tickerIdx = header.indexOfFirst { it.contains("ticker") || it.contains("symbol") || it.contains("code") }
        val nameIdx = header.indexOfFirst { it.contains("name") || it.contains("company") || it.contains("desc") }
        val priceIdx = header.indexOfFirst { it.contains("price") || it.contains("ltp") || it.contains("cmp") || it.contains("close") }
        val changeIdx = header.indexOfFirst { it.contains("change") || it.contains("chg") || it.contains("%") }
        val volIdx = header.indexOfFirst { it.contains("volume") || it.contains("vol") || it.contains("qty") }
        val lowIdx = header.indexOfFirst { it.contains("low") || it.contains("52w l") || it.contains("min") }
        val highIdx = header.indexOfFirst { it.contains("high") || it.contains("52w h") || it.contains("max") }
        val upsideIdx = header.indexOfFirst { it.contains("upside") || it.contains("potential") || it.contains("target") }
        val peIdx = header.indexOfFirst { it.contains("pe") || it.contains("p/e") || it.contains("valuation") }
        val capIdx = header.indexOfFirst { it.contains("cap") || it.contains("size") || it.contains("market") }
        val growthIdx = header.indexOfFirst { it.contains("growth") || it.contains("rate") || it.contains("eps") }

        if (tickerIdx == -1) return null

        val list = mutableListOf<DoublerItem>()
        for (i in 1 until rows.size) {
            val row = rows[i]
            if (row.size <= tickerIdx || row[tickerIdx].isBlank()) continue
            val ticker = row[tickerIdx]
            val name = if (nameIdx != -1 && row.size > nameIdx) row[nameIdx] else ticker
            val price = if (priceIdx != -1 && row.size > priceIdx) cleanDouble(row[priceIdx]) else 0.0
            val chg = if (changeIdx != -1 && row.size > changeIdx) cleanDouble(row[changeIdx]) else 0.0
            val vol = if (volIdx != -1 && row.size > volIdx) row[volIdx] else "N/A"
            val low = if (lowIdx != -1 && row.size > lowIdx) cleanDouble(row[lowIdx]) else price * 0.5
            val high = if (highIdx != -1 && row.size > highIdx) cleanDouble(row[highIdx]) else price * 1.5
            val upside = if (upsideIdx != -1 && row.size > upsideIdx) cleanDouble(row[upsideIdx]) else 100.0
            val pe = if (peIdx != -1 && row.size > peIdx) cleanDouble(row[peIdx]) else 25.0
            val cap = if (capIdx != -1 && row.size > capIdx) row[capIdx] else "Mid Cap"
            val growth = if (growthIdx != -1 && row.size > growthIdx) cleanDouble(row[growthIdx]) else 25.0

            list.add(DoublerItem(ticker, name, price, chg, vol, low, high, upside, pe, cap, growth))
        }
        return list
    }
}
