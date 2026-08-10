package com.abhibot.sevenone.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhibot.sevenone.data.DhanCredentialsStore
import com.abhibot.sevenone.data.DhanMarketFeed
import com.abhibot.sevenone.data.MarketRepository
import com.abhibot.sevenone.data.SecureApiKeyStore
import kotlinx.coroutines.launch

private val Bg = Color(0xFF080C12)
private val Panel = Color(0xFF111824)
private val Panel2 = Color(0xFF182230)
private val White = Color(0xFFF4F7FB)
private val Muted = Color(0xFF8D99A8)
private val Blue = Color(0xFF4F8CFF)
private val Green = Color(0xFF22C55E)
private val Red = Color(0xFFEF4444)

@Composable
fun AbhiBoTTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Bg,
            surface = Panel,
            primary = Blue,
            onBackground = White,
            onSurface = White
        ),
        content = content
    )
}

private enum class Screen {
    Home,
    Markets,
    Trade,
    Orders,
    Profile,
    Connections
}

@Composable
fun AbhiBoTApp(
    repository: MarketRepository,
    keyStore: SecureApiKeyStore
) {

    var loggedIn by remember {
        mutableStateOf(false)
    }

    var screen by remember {
        mutableStateOf(Screen.Home)
    }

    if (!loggedIn) {

        LoginScreen(
            onLogin = {
                loggedIn = true
                screen = Screen.Home
            },
            onConnections = {
                loggedIn = true
                screen = Screen.Connections
            }
        )

        return
    }

    Scaffold(
        containerColor = Bg,

        bottomBar = {

            NavigationBar(
                containerColor = Panel
            ) {

                val tabs = listOf(
                    Screen.Home to "Home",
                    Screen.Markets to "Markets",
                    Screen.Trade to "Trade",
                    Screen.Orders to "Orders",
                    Screen.Profile to "Profile"
                )

                tabs.forEach { (target, label) ->

                    NavigationBarItem(
                        selected = screen == target,

                        onClick = {
                            screen = target
                        },

                        icon = {

                            Text(
                                when (target) {
                                    Screen.Home -> "⌂"
                                    Screen.Markets -> "⌁"
                                    Screen.Trade -> "+"
                                    Screen.Orders -> "☷"
                                    Screen.Profile -> "●"
                                    else -> "•"
                                },
                                fontSize = 20.sp
                            )
                        },

                        label = {
                            Text(
                                label,
                                fontSize = 10.sp
                            )
                        }
                    )
                }
            }
        }
    ) { padding ->

        Box(
            Modifier.padding(padding)
        ) {

            when (screen) {

                Screen.Home -> {

                    HomeScreen(
                        onConnections = {
                            screen = Screen.Connections
                        },
                        onMarkets = {
                            screen = Screen.Markets
                        },
                        onTrade = {
                            screen = Screen.Trade
                        }
                    )
                }

                Screen.Markets -> {

                    MarketsScreen(
                        repository = repository,
                        keyStore = keyStore
                    )
                }

                Screen.Trade -> {

                    TradeScreen()
                }

                Screen.Orders -> {

                    OrdersScreen()
                }

                Screen.Profile -> {

                    ProfileScreen(
                        onConnections = {
                            screen = Screen.Connections
                        },
                        onLogout = {
                            loggedIn = false
                            screen = Screen.Home
                        }
                    )
                }

                Screen.Connections -> {

                    ConnectionsScreen(
                        repository = repository,
                        keyStore = keyStore
                    )
                }
            }
        }
    }
}


/* =========================================================
   LOGIN
   ========================================================= */

@Composable
private fun LoginScreen(
    onLogin: () -> Unit,
    onConnections: () -> Unit
) {

    var username by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .padding(22.dp),

        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(
            Modifier.height(55.dp)
        )

        Text(
            text = "AbhiBoT",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = White
        )

        Text(
            text = "7.1  •  ADVANCED VIRTUAL TRADING",
            color = Blue,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            Modifier.height(35.dp)
        )

        Card(
            colors = CardDefaults.cardColors(
                containerColor = Panel
            ),
            shape = RoundedCornerShape(22.dp)
        ) {

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {

                Text(
                    text = "Welcome back",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Login to your paper-trading workspace",
                    color = Muted,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(
            Modifier.height(18.dp)
        )

        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
            },
            label = {
                Text("Username / Email")
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(
            Modifier.height(10.dp)
        )

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
            },
            label = {
                Text("Password")
            },
            singleLine = true,
            visualTransformation =
                PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(
            Modifier.height(16.dp)
        )

        Button(
            onClick = onLogin,

            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),

            shape = RoundedCornerShape(15.dp)
        ) {

            Text(
                "LOGIN",
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(
            Modifier.height(10.dp)
        )

        OutlinedButton(
            onClick = onConnections,

            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {

            Text(
                "API / BROKER CONNECTIONS"
            )
        }

        Spacer(
            Modifier.height(25.dp)
        )

        Text(
            "PAPER TRADING",
            color = Green,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )

        Text(
            "Real broker orders are disabled in this mode.",
            color = Muted,
            fontSize = 10.sp
        )
    }
}


/* =========================================================
   HOME
   ========================================================= */

@Composable
private fun HomeScreen(
    onConnections: () -> Unit,
    onMarkets: () -> Unit,
    onTrade: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .verticalScroll(
                rememberScrollState()
            )
            .padding(18.dp)
    ) {

        Header(
            title = "AbhiBoT 7.1",
            subtitle = "ADVANCED PAPER TRADING"
        )

        Card(
            colors = CardDefaults.cardColors(
                containerColor = Panel
            ),
            shape = RoundedCornerShape(22.dp)
        ) {

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {

                Text(
                    "Virtual Balance",
                    color = Muted,
                    fontSize = 12.sp
                )

                Text(
                    "₹1,00,000.00",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "Today  +₹2,840.50  (+2.91%)",
                    color = Green,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(
            Modifier.height(18.dp)
        )

        Text(
            "Market Overview",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            Modifier.height(8.dp)
        )

        MarketRow(
            "NIFTY 50",
            "24,836.20",
            "+0.72%"
        )

        MarketRow(
            "BANK NIFTY",
            "56,412.45",
            "+0.48%"
        )

        MarketRow(
            "SENSEX",
            "81,986.12",
            "-0.18%"
        )

        Spacer(
            Modifier.height(18.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {

            Button(
                onClick = onMarkets,
                modifier = Modifier.weight(1f)
            ) {

                Text("MARKETS")
            }

            Button(
                onClick = onTrade,
                modifier = Modifier.weight(1f),

                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = Green
                    )
            ) {

                Text("TRADE")
            }
        }

        Spacer(
            Modifier.height(10.dp)
        )

        OutlinedButton(
            onClick = onConnections,
            modifier = Modifier.fillMaxWidth()
        ) {

            Text(
                "LIVE DATA & BROKERS"
            )
        }
    }
}


/* =========================================================
   MARKETS
   ========================================================= */

@Composable
private fun MarketsScreen(
    repository: MarketRepository,
    keyStore: SecureApiKeyStore
) {

    var symbol by remember {
        mutableStateOf("AAPL")
    }

    var status by remember {
        mutableStateOf("Ready")
    }

    var price by remember {
        mutableStateOf<Double?>(null)
    }

    val scope =
        rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .verticalScroll(
                rememberScrollState()
            )
            .padding(18.dp)
    ) {

        Header(
            "Markets",
            "TWELVE DATA REST"
        )

        OutlinedTextField(
            value = symbol,

            onValueChange = {
                symbol = it
            },

            label = {
                Text("Symbol")
            },

            singleLine = true,

            modifier =
                Modifier.fillMaxWidth()
        )

        Spacer(
            Modifier.height(10.dp)
        )

        Button(
            onClick = {

                val apiKey =
                    keyStore.read()

                if (apiKey.isNullOrBlank()) {

                    status =
                        "Add Twelve Data API key in Connections"

                } else {

                    scope.launch {

                        status =
                            "Updating..."

                        repository
                            .getPrice(
                                symbol.trim(),
                                apiKey
                            )
                            .onSuccess {

                                price =
                                    it.price

                                status =
                                    "Live REST quote updated"
                            }
                            .onFailure {

                                status =
                                    it.message
                                        ?: "Request failed"
                            }
                    }
                }
            },

            modifier =
                Modifier.fillMaxWidth()
        ) {

            Text(
                "REFRESH LIVE PRICE"
            )
        }

        Spacer(
            Modifier.height(15.dp)
        )

        if (price != null) {

            Card(
                colors =
                    CardDefaults.cardColors(
                        containerColor = Panel
                    ),

                shape =
                    RoundedCornerShape(18.dp)
            ) {

                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {

                    Text(
                        symbol.uppercase(),
                        color = Muted,
                        fontSize = 12.sp
                    )

                    Text(
                        "%.2f".format(price),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        status,
                        color = Green,
                        fontSize = 11.sp
                    )
                }
            }

        } else {

            Text(
                status,
                color = Muted,
                fontSize = 12.sp
            )
        }

        Spacer(
            Modifier.height(20.dp)
        )

        Text(
            "Indian Indices",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        MarketRow(
            "NIFTY 50",
            "Dhan Live Feed",
            "IDX_I / 13"
        )

        MarketRow(
            "BANK NIFTY",
            "Dhan Live Feed",
            "IDX_I / 25"
        )

        MarketRow(
            "SENSEX",
            "Dhan Live Feed",
            "IDX_I / 51"
        )
    }
}


/* =========================================================
   TRADE
   ========================================================= */

@Composable
private fun TradeScreen() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .verticalScroll(
                rememberScrollState()
            )
            .padding(18.dp)
    ) {

        Header(
            "Start Trade",
            "PAPER ORDER"
        )

        OutlinedTextField(
            value = "NIFTY 50",
            onValueChange = {},
            label = {
                Text("Symbol")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(
            Modifier.height(10.dp)
        )

        OutlinedTextField(
            value = "50",
            onValueChange = {},
            label = {
                Text("Quantity")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(
            Modifier.height(10.dp)
        )

        OutlinedTextField(
            value = "24836.20",
            onValueChange = {},
            label = {
                Text("Entry Price")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(
            Modifier.height(15.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {

            Button(
                onClick = {},

                modifier =
                    Modifier.weight(1f),

                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = Green
                    )
            ) {

                Text("BUY")
            }

            Button(
                onClick = {},

                modifier =
                    Modifier.weight(1f),

                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = Red
                    )
            ) {

                Text("SELL")
            }
        }

        Spacer(
            Modifier.height(15.dp)
        )

        Text(
            "Paper order preview",
            color = Muted,
            fontSize = 12.sp
        )

        Text(
            "No broker order will be submitted.",
            color = White,
            fontWeight = FontWeight.Bold
        )
    }
}


/* =========================================================
   ORDERS
   ========================================================= */

@Composable
private fun OrdersScreen() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .padding(18.dp)
    ) {

        Header(
            "Orders & Positions",
            "PAPER TRADING"
        )

        Card(
            colors =
                CardDefaults.cardColors(
                    containerColor = Panel
                ),

            shape =
                RoundedCornerShape(18.dp)
        ) {

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {

                Text(
                    "No open positions",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )

                Text(
                    "Your paper trades will appear here.",
                    color = Muted,
                    fontSize = 12.sp
                )
            }
        }
    }
}


/* =========================================================
   PROFILE
   ========================================================= */

@Composable
private fun ProfileScreen(
    onConnections: () -> Unit,
    onLogout: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .verticalScroll(
                rememberScrollState()
            )
            .padding(18.dp)
    ) {

        Header(
            "Profile",
            "ACCOUNT"
        )

        Card(
            colors =
                CardDefaults.cardColors(
                    containerColor = Panel
                ),

            shape =
                RoundedCornerShape(20.dp)
        ) {

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {

                Text(
                    "AbhiBoT Trader",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "Virtual account • ₹1,00,000",
                    color = Muted,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(
            Modifier.height(14.dp)
        )

        OutlinedButton(
            onClick = onConnections,
            modifier = Modifier.fillMaxWidth()
        ) {

            Text(
                "CONNECTION SETTINGS"
            )
        }

        Spacer(
            Modifier.height(10.dp)
        )

        Button(
            onClick = onLogout,

            modifier =
                Modifier.fillMaxWidth(),

            colors =
                ButtonDefaults.buttonColors(
                    containerColor = Red
                )
        ) {

            Text(
                "LOG OUT"
            )
        }
    }
}


/* =========================================================
   CONNECTIONS
   ========================================================= */

@Composable
private fun ConnectionsScreen(
    repository: MarketRepository,
    keyStore: SecureApiKeyStore
) {

    val context =
        LocalContext.current

    val dhanStore =
        remember {
            DhanCredentialsStore(context)
        }

    val feed =
        remember {
            DhanMarketFeed()
        }

    val feedStatus by
        feed.status.collectAsState()

    val quotes by
        feed.quotes.collectAsState()

    var twelveKey by remember {

        mutableStateOf(
            keyStore.read() ?: ""
        )
    }

    var clientId by remember {

        mutableStateOf(
            dhanStore.getClientId() ?: ""
        )
    }

    var accessToken by remember {

        mutableStateOf(
            dhanStore.getAccessToken() ?: ""
        )
    }

    var message by remember {
        mutableStateOf("")
    }

    DisposableEffect(Unit) {

        onDispose {
            feed.disconnect()
        }
    }

    val instruments =
        listOf(
            "NIFTY 50" to "13",
            "BANK NIFTY" to "25",
            "SENSEX" to "51"
        )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .verticalScroll(
                rememberScrollState()
            )
            .padding(18.dp)
    ) {

        Header(
            "Connections",
            "LIVE MARKET DATA + BROKERS"
        )

        /* DATA STREAM */

        Card(
            colors =
                CardDefaults.cardColors(
                    containerColor = Panel
                ),

            shape =
                RoundedCornerShape(18.dp)
        ) {

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {

                Text(
                    "● DATA STREAM",
                    color = Blue,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    Modifier.height(5.dp)
                )

                Text(
                    feedStatus,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "Dhan WebSocket • Live Market Feed",
                    color = Muted,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(
            Modifier.height(20.dp)
        )

        /* TWELVE DATA */

        Text(
            "Twelve Data API Key",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            Modifier.height(7.dp)
        )

        OutlinedTextField(
            value = twelveKey,

            onValueChange = {
                twelveKey = it
            },

            label = {
                Text("API Key")
            },

            modifier =
                Modifier.fillMaxWidth(),

            singleLine = true,

            visualTransformation =
                PasswordVisualTransformation()
        )

        Spacer(
            Modifier.height(8.dp)
        )

        Button(
            onClick = {

                if (twelveKey.isBlank()) {

                    message =
                        "Please enter Twelve Data API key"

                } else {

                    keyStore.save(
                        twelveKey.trim()
                    )

                    message =
                        "Twelve Data API key saved securely"
                }
            },

            modifier =
                Modifier.fillMaxWidth()
        ) {

            Text(
                "SAVE SECURELY"
            )
        }

        Spacer(
            Modifier.height(22.dp)
        )

        /* DHAN */

        Text(
            "Dhan Connection",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            Modifier.height(7.dp)
        )

        OutlinedTextField(
            value = clientId,

            onValueChange = {
                clientId = it
            },

            label = {
                Text("Dhan Client ID")
            },

            modifier =
                Modifier.fillMaxWidth(),

            singleLine = true
        )

        Spacer(
            Modifier.height(10.dp)
        )

        OutlinedTextField(
            value = accessToken,

            onValueChange = {
                accessToken = it
            },

            label = {
                Text("Dhan Access Token")
            },

            modifier =
                Modifier.fillMaxWidth(),

            singleLine = true,

            visualTransformation =
                PasswordVisualTransformation()
        )

        Spacer(
            Modifier.height(10.dp)
        )

        Button(
            onClick = {

                if (
                    clientId.isBlank() ||
                    accessToken.isBlank()
                ) {

                    message =
                        "Client ID and Access Token are required"

                } else {

                    dhanStore.saveCredentials(
                        clientId,
                        accessToken
                    )

                    feed.connect(
                        clientId = clientId.trim(),
                        accessToken = accessToken.trim(),

                        instruments =
                            instruments.map {
                                "IDX_I" to it.second
                            }
                    )

                    message =
                        "Dhan credentials saved. Connecting..."
                }
            },

            modifier =
                Modifier.fillMaxWidth()
        ) {

            Text(
                "SAVE & CONNECT DHAN"
            )
        }

        Spacer(
            Modifier.height(8.dp)
        )

        OutlinedButton(
            onClick = {

                feed.disconnect()

                message =
                    "Dhan feed disconnected"
            },

            modifier =
                Modifier.fillMaxWidth()
        ) {

            Text(
                "DISCONNECT DHAN"
            )
        }

        Spacer(
            Modifier.height(22.dp)
        )

        /* LIVE QUOTES */

        Text(
            "Live Quotes",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            Modifier.height(5.dp)
        )

        instruments.forEach { (name, id) ->

            val quote =
                quotes[id]

            MarketRow(
                name = name,

                value =
                    quote?.price?.let {
                        "%.2f".format(it)
                    } ?: "--",

                change =
                    if (quote == null)
                        "WAITING"
                    else
                        "LIVE"
            )
        }

        Spacer(
            Modifier.height(10.dp)
        )

        if (message.isNotBlank()) {

            Text(
                message,
                color = Green,
                fontSize = 11.sp
            )
        }

        Spacer(
            Modifier.height(10.dp)
        )

        Text(
            "Credentials are encrypted using Android Keystore. Never commit real API keys or tokens to GitHub.",
            color = Muted,
            fontSize = 10.sp
        )
    }
}


/* =========================================================
   HEADER
   ========================================================= */

@Composable
private fun Header(
    title: String,
    subtitle: String
) {

    Column(
        Modifier.padding(
            bottom = 16.dp
        )
    ) {

        Text(
            title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = White
        )

        Text(
            subtitle,
            color = Muted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}


/* =========================================================
   MARKET ROW
   ========================================================= */

@Composable
private fun MarketRow(
    name: String,
    value: String,
    change: String
) {

    Card(
        colors =
            CardDefaults.cardColors(
                containerColor = Panel2
            ),

        shape =
            RoundedCornerShape(16.dp),

        modifier =
            Modifier.padding(
                vertical = 4.dp
            )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 14.dp
                ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Column(
                Modifier.weight(1f)
            ) {

                Text(
                    name,
                    fontWeight =
                        FontWeight.Bold
                )

                Text(
                    value,
                    color = Muted,
                    fontSize = 11.sp
                )
            }

            Text(
                change,

                color =
                    if (change.startsWith("-"))
                        Red
                    else if (
                        change == "LIVE"
                    )
                        Green
                    else
                        Blue,

                fontWeight =
                    FontWeight.Bold,

                fontSize = 11.sp
            )
        }
    }
}
