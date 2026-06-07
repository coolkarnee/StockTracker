package com.example.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.net.URLEncoder

class StockRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    companion object {
        const val DEFAULT_NSE_ID = "1qDmp7dz8bnNLPxRVvsiNp08Wfp1Eks7AykckukNltbw"
        const val DEFAULT_NASDAQ_ID = "1FbixW3abIA-G0nK7WOFN6gjoSTjumzZlM43MWcbpHhU"

        fun extractId(input: String): String {
            val trimmed = input.trim()
            if (trimmed.contains("docs.google.com/spreadsheets")) {
                val eMatch = "docs\\.google\\.com/spreadsheets/d/e/([a-zA-Z0-9-_]+)".toRegex().find(trimmed)
                if (eMatch != null) {
                    return eMatch.groupValues[1]
                }
                val dMatch = "docs\\.google\\.com/spreadsheets/d/([a-zA-Z0-9-_]+)".toRegex().find(trimmed)
                if (dMatch != null) {
                    return dMatch.groupValues[1]
                }
            }
            return trimmed
        }
    }

    var nseId: String = DEFAULT_NSE_ID
        set(value) {
            field = extractId(value)
        }

    var nasdaqId: String = DEFAULT_NASDAQ_ID
        set(value) {
            field = extractId(value)
        }

    private suspend fun fetchCsvContent(spreadsheetId: String, tabName: String): String? = withContext(Dispatchers.IO) {
        val encodedTab = URLEncoder.encode(tabName, "UTF-8")
        // Primary URL: Export format (adaptive based on whether it is a published web sheet)
        val url = if (spreadsheetId.startsWith("2PACX-")) {
            "https://docs.google.com/spreadsheets/d/e/$spreadsheetId/pub?output=csv&sheet=$encodedTab"
        } else {
            "https://docs.google.com/spreadsheets/d/$spreadsheetId/export?format=csv&sheet=$encodedTab"
        }
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrBlank()) {
                        return@withContext body
                    }
                } else {
                    Log.w("StockRepository", "Primary fetch failed for $tabName: code=${response.code}")
                }
            }
        } catch (e: Exception) {
            Log.e("StockRepository", "Exception in primary fetch for $tabName", e)
        }

        // Secondary URL: Gviz query API or pub API fallback
        val fallbackUrl = if (spreadsheetId.startsWith("2PACX-")) {
            "https://docs.google.com/spreadsheets/d/e/$spreadsheetId/pub?output=csv&sheet=$encodedTab"
        } else {
            "https://docs.google.com/spreadsheets/d/$spreadsheetId/gviz/tq?tqx=out:csv&sheet=$encodedTab"
        }
        val fallbackRequest = Request.Builder()
            .url(fallbackUrl)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36")
            .build()

        try {
            client.newCall(fallbackRequest).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrBlank()) {
                        return@withContext body
                    }
                } else {
                    Log.w("StockRepository", "Fallback fetch failed for $tabName: code=${response.code}")
                }
            }
        } catch (e: Exception) {
            Log.e("StockRepository", "Exception in fallback fetch for $tabName", e)
        }

        return@withContext null
    }

    suspend fun getIndustryAnalysis(isNse: Boolean): List<IndustryPerformance> = withContext(Dispatchers.IO) {
        val sheetId = if (isNse) nseId else nasdaqId
        val csv = fetchCsvContent(sheetId, "Industry Analysis")
        if (csv != null) {
            val parsed = SpreadsheetParser.parseIndustryAnalysis(csv)
            if (!parsed.isNullOrEmpty()) {
                return@withContext parsed
            }
        }
        // Fallback
        return@withContext if (isNse) FallbackData.nseIndustries else FallbackData.nasdaqIndustries
    }

    suspend fun getLiquidLeaders(isNse: Boolean): List<LiquidLeader> = withContext(Dispatchers.IO) {
        val sheetId = if (isNse) nseId else nasdaqId
        val csv = fetchCsvContent(sheetId, "Liquid Momentum Leaders")
        if (csv != null) {
            val parsed = SpreadsheetParser.parseLiquidLeaders(csv)
            if (!parsed.isNullOrEmpty()) {
                return@withContext parsed
            }
        }
        // Fallback
        return@withContext if (isNse) FallbackData.nseLiquidLeaders else FallbackData.nasdaqLiquidLeaders
    }

    suspend fun getMomentumTopNames(isNse: Boolean): List<MomentumTopName> = withContext(Dispatchers.IO) {
        val sheetId = if (isNse) nseId else nasdaqId
        val csv = fetchCsvContent(sheetId, "Momentum Top Names")
        if (csv != null) {
            val parsed = SpreadsheetParser.parseMomentumTopNames(csv)
            if (!parsed.isNullOrEmpty()) {
                return@withContext parsed
            }
        }
        // Fallback
        return@withContext if (isNse) FallbackData.nseMomentumTopNames else FallbackData.nasdaqMomentumTopNames
    }

    suspend fun getDoublers(isNse: Boolean): List<DoublerItem> = withContext(Dispatchers.IO) {
        val sheetId = if (isNse) nseId else nasdaqId
        val csv = fetchCsvContent(sheetId, "Doublers")
        if (csv != null) {
            val parsed = SpreadsheetParser.parseDoublers(csv)
            if (!parsed.isNullOrEmpty()) {
                return@withContext parsed
            }
        }
        // Fallback
        return@withContext if (isNse) FallbackData.nseDoublers else FallbackData.nasdaqDoublers
    }

    // Check if network fetch works or fails (for UI status indication)
    suspend fun checkNetworkStatus(isNse: Boolean): Boolean {
        val sheetId = if (isNse) nseId else nasdaqId
        return testSheetConnection(sheetId)
    }

    suspend fun testSheetConnection(sheetId: String): Boolean = withContext(Dispatchers.IO) {
        val cleanedId = extractId(sheetId)
        val encodedTab = URLEncoder.encode("Industry Analysis", "UTF-8")
        val url = if (cleanedId.startsWith("2PACX-")) {
            "https://docs.google.com/spreadsheets/d/e/$cleanedId/pub?output=csv&sheet=$encodedTab"
        } else {
            "https://docs.google.com/spreadsheets/d/$cleanedId/export?format=csv&sheet=$encodedTab"
        }
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36")
            .build()
        try {
            client.newCall(request).execute().use { response ->
                return@withContext response.isSuccessful
            }
        } catch (e: Exception) {
            return@withContext false
        }
    }
}
