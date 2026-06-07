package com.example

import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Test
import java.io.File
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter

class SpreadsheetInspector {

    private val client = OkHttpClient()

    private val nseId = "1qDmp7dz8bnNLPxRVvsiNp08Wfp1Eks7AykckukNltbw"
    private val nasdaqId = "1FbixW3abIA-G0nK7WOFN6gjoSTjumzZlM43MWcbpHhU"

    private val tabs = listOf(
        "Industry Analysis",
        "Liquid Momentum Leaders",
        "Momentum Top Names",
        "Doublers"
    )

    private val outputFile = File("src/test/java/com/example/SpreadsheetInspector_output.txt")

    private fun downloadSheet(spreadsheetId: String, tabName: String): String? {
        val encodedTab = java.net.URLEncoder.encode(tabName, "UTF-8")
        val exportUrl = "https://docs.google.com/spreadsheets/d/$spreadsheetId/export?format=csv&sheet=$encodedTab"
        val request = Request.Builder()
            .url(exportUrl)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko)")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    return response.body?.string()
                } else {
                    val fallbackUrl = "https://docs.google.com/spreadsheets/d/$spreadsheetId/gviz/tq?tqx=out:csv&sheet=$encodedTab"
                    val fallbackRequest = Request.Builder()
                        .url(fallbackUrl)
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko)")
                        .build()
                    client.newCall(fallbackRequest).execute().use { fbResponse ->
                        if (fbResponse.isSuccessful) {
                            return fbResponse.body?.string()
                        } else {
                            outputFile.appendText("Failed: Response codes: ${response.code} and ${fbResponse.code}\n")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            val sw = StringWriter()
            e.printStackTrace(PrintWriter(sw))
            outputFile.appendText("Exception in downloadSheet: ${sw.toString()}\n")
        }
        return null
    }

    @Test
    fun testInspect() {
        outputFile.writeText("=== SPREADSHEET INSPECTION RESULTS ===\n\n")

        outputFile.appendText("=== NSE DATABASE ===\n")
        for (tab in tabs) {
            outputFile.appendText("\n--- NSE TAB: $tab ---\n")
            val content = downloadSheet(nseId, tab)
            if (content != null) {
                val lines = content.split("\n")
                outputFile.appendText("Total rows: ${lines.size}\n")
                lines.take(20).forEachIndexed { i, line ->
                    outputFile.appendText("[$i] $line\n")
                }
            } else {
                outputFile.appendText("Could not load tab: $tab\n")
            }
        }
    }
}
