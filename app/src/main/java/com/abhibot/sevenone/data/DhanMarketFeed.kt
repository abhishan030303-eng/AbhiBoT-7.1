package com.abhibot.sevenone.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.json.JSONArray
import org.json.JSONObject
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.TimeUnit

class DhanMarketFeed {

    data class LiveQuote(
        val securityId: String,
        val price: Double,
        val previousClose: Double? = null,
        val lastTradeTime: Long? = null
    )

    private val client = OkHttpClient.Builder()
        .pingInterval(20, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null

    private val _status = MutableStateFlow("Disconnected")
    val status: StateFlow<String> = _status

    private val _quotes =
        MutableStateFlow<Map<String, LiveQuote>>(emptyMap())

    val quotes: StateFlow<Map<String, LiveQuote>> = _quotes

    fun connect(
        clientId: String,
        accessToken: String,
        instruments: List<Pair<String, String>>
    ) {
        disconnect()

        if (clientId.isBlank() || accessToken.isBlank()) {
            _status.value = "Client ID and Access Token required"
            return
        }

        if (instruments.isEmpty()) {
            _status.value = "No instruments configured"
            return
        }

        val url =
            "wss://api-feed.dhan.co" +
                    "?version=2" +
                    "&token=${java.net.URLEncoder.encode(accessToken.trim(), "UTF-8")}" +
                    "&clientId=${java.net.URLEncoder.encode(clientId.trim(), "UTF-8")}" +
                    "&authType=2"

        _status.value = "Connecting..."

        val request = Request.Builder()
            .url(url)
            .build()

        webSocket = client.newWebSocket(
            request,
            object : WebSocketListener() {

                override fun onOpen(
                    webSocket: WebSocket,
                    response: Response
                ) {
                    _status.value = "Connected • subscribing"

                    subscribeTicker(
                        webSocket,
                        instruments
                    )
                }

                override fun onMessage(
                    webSocket: WebSocket,
                    bytes: ByteString
                ) {
                    parseBinaryPacket(bytes.toByteArray())
                }

                override fun onMessage(
                    webSocket: WebSocket,
                    text: String
                ) {
                    if (text.isNotBlank()) {
                        _status.value = "Server: ${text.take(100)}"
                    }
                }

                override fun onClosing(
                    webSocket: WebSocket,
                    code: Int,
                    reason: String
                ) {
                    _status.value = "Closing..."
                }

                override fun onClosed(
                    webSocket: WebSocket,
                    code: Int,
                    reason: String
                ) {
                    this@DhanMarketFeed.webSocket = null
                    _status.value = "Disconnected"
                }

                override fun onFailure(
                    webSocket: WebSocket,
                    t: Throwable,
                    response: Response?
                ) {
                    this@DhanMarketFeed.webSocket = null
                    _status.value =
                        "Connection error: ${t.message ?: "Unknown error"}"
                }
            }
        )
    }

    private fun subscribeTicker(
        webSocket: WebSocket,
        instruments: List<Pair<String, String>>
    ) {
        val list = JSONArray()

        instruments.take(100).forEach { (exchangeSegment, securityId) ->

            val instrument = JSONObject()
                .put("ExchangeSegment", exchangeSegment)
                .put("SecurityId", securityId)

            list.put(instrument)
        }

        val request = JSONObject()
            .put("RequestCode", 15)
            .put("InstrumentCount", list.length())
            .put("InstrumentList", list)

        webSocket.send(request.toString())

        _status.value = "Connected • Live Feed"
    }

    private fun parseBinaryPacket(bytes: ByteArray) {

        var offset = 0

        while (offset + 8 <= bytes.size) {

            try {

                val buffer = ByteBuffer
                    .wrap(bytes, offset, bytes.size - offset)
                    .order(ByteOrder.LITTLE_ENDIAN)

                val responseCode =
                    buffer.get(0).toInt() and 0xFF

                val messageLength =
                    buffer.getShort(1).toInt() and 0xFFFF

                val securityId =
                    (buffer.getInt(4).toLong() and 0xFFFFFFFFL)
                        .toString()

                if (messageLength < 8) {
                    return
                }

                if (offset + messageLength > bytes.size) {
                    return
                }

                when (responseCode) {

                    // Ticker Packet
                    2 -> {

                        if (messageLength >= 16) {

                            val price =
                                buffer.getFloat(8).toDouble()

                            val ltt =
                                buffer.getInt(12).toLong()

                            updateQuote(
                                securityId = securityId,
                                price = price,
                                previousClose = null,
                                lastTradeTime = ltt
                            )
                        }
                    }

                    // Previous Close Packet
                    6 -> {

                        if (messageLength >= 16) {

                            val previousClose =
                                buffer.getFloat(8).toDouble()

                            val existing =
                                _quotes.value[securityId]

                            _quotes.value =
                                _quotes.value.toMutableMap().apply {

                                    put(
                                        securityId,
                                        LiveQuote(
                                            securityId = securityId,
                                            price =
                                                existing?.price
                                                    ?: previousClose,
                                            previousClose =
                                                previousClose,
                                            lastTradeTime =
                                                existing?.lastTradeTime
                                        )
                                    )
                                }
                        }
                    }

                    // Feed disconnected
                    50 -> {
                        _status.value =
                            "Dhan feed disconnected"
                    }
                }

                offset += messageLength

            } catch (e: Exception) {

                _status.value =
                    "Packet error: ${e.message ?: "Invalid packet"}"

                return
            }
        }
    }

    private fun updateQuote(
        securityId: String,
        price: Double,
        previousClose: Double?,
        lastTradeTime: Long?
    ) {

        val old =
            _quotes.value[securityId]

        val quote =
            LiveQuote(
                securityId = securityId,
                price = price,
                previousClose =
                    previousClose ?: old?.previousClose,
                lastTradeTime =
                    lastTradeTime ?: old?.lastTradeTime
            )

        _quotes.value =
            _quotes.value.toMutableMap().apply {
                put(securityId, quote)
            }
    }

    fun disconnect() {

        webSocket?.close(
            1000,
            "User requested disconnect"
        )

        webSocket = null

        _status.value = "Disconnected"
    }
}
