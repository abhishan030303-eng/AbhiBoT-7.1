package com.abhibot.sevenone.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

data class Quote(
    val symbol: String,
    val price: Double,
    val change: Double? = null,
    val percentChange: Double? = null,
    val timestamp: Long? = null
)

class MarketRepository {
    private val client = OkHttpClient()

    suspend fun getPrice(symbol: String, apiKey: String): Result<Quote> =
        withContext(Dispatchers.IO) {
            try {
                val url = "https://api.twelvedata.com/price?symbol=${java.net.URLEncoder.encode(symbol, "UTF-8")}"
                val request = Request.Builder()
                    .url(url)
                    .header("Authorization", "apikey $apiKey")
                    .build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext Result.failure(
                            IllegalStateException("HTTP ${response.code}")
                        )
                    }
                    val body = response.body?.string() ?: error("Empty response")
                    val json = JSONObject(body)
                    if (json.has("status") && json.optString("status") == "error") {
                        return@withContext Result.failure(
                            IllegalStateException(json.optString("message", "Provider error"))
                        )
                    }
                    Result.success(
                        Quote(
                            symbol = json.optString("symbol", symbol),
                            price = json.getDouble("price")
                        )
                    )
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
