package com.abhibot.sevenone.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhibot.sevenone.data.MarketRepository
import com.abhibot.sevenone.data.SecureApiKeyStore
import kotlinx.coroutines.launch

private val Bg = Color(0xFF0B0F14)
private val Panel = Color(0xFF121821)
private val Panel2 = Color(0xFF171F2A)
private val Text = Color(0xFFF4F7FA)
private val Muted = Color(0xFF8E9AA8)
private val Green = Color(0xFF22C55E)
private val Red = Color(0xFFEF4444)
private val Blue = Color(0xFF4F8CFF)
private val Border = Color(0xFF26313D)

@Composable
fun AbhiBoTTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Bg, surface = Panel, primary = Blue,
            onBackground = Text, onSurface = Text
        ),
        content = content
    )
}

private enum class Screen { Login, Home, Markets, Trade, Orders, Profile, Connections }

@Composable
fun AbhiBoTApp(repository: MarketRepository, keyStore: SecureApiKeyStore) {
    var screen by remember { mutableStateOf(Screen.Login) }
    var loggedIn by remember { mutableStateOf(false) }

    if (!loggedIn) {
        LoginScreen(
            onLogin = { loggedIn = true; screen = Screen.Home },
            onApi = { screen = Screen.Connections }
        )
        return
    }

    Scaffold(
        containerColor = Bg,
        bottomBar = {
            NavigationBar(containerColor = Panel) {
                listOf(
                    Screen.Home to "Home",
                    Screen.Markets to "Markets",
                    Screen.Trade to "Trade",
                    Screen.Orders to "Orders",
                    Screen.Profile to "Profile"
                ).forEach { (s, label) ->
                    NavigationBarItem(
                        selected = screen == s,
                        onClick = { screen = s },
                        icon = { Icon(
                            when (s) {
                                Screen.Home -> Icons.Default.Home
                                Screen.Markets -> Icons.Default.ShowChart
                                Screen.Trade -> Icons.Default.AddCircle
                                Screen.Orders -> Icons.Default.List
                                Screen.Profile -> Icons.Default.Person
                                else -> Icons.Default.Home
                            }, label
                        ) },
                        label = { Text(label, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            indicatorColor = Blue,
                            unselectedIconColor = Muted,
                            unselectedTextColor = Muted
                        )
                    )
                }
            }
        }
    ) { pad ->
        Box(Modifier.padding(pad)) {
            when (screen) {
                Screen.Home -> HomeScreen(onConnections = { screen = Screen.Connections })
                Screen.Markets -> MarketsScreen(repository, keyStore)
                Screen.Trade -> TradeScreen()
                Screen.Orders -> OrdersScreen()
                Screen.Profile -> ProfileScreen(
                    onConnections = { screen = Screen.Connections },
                    onLogout = { loggedIn = false; screen = Screen.Login }
                )
                Screen.Connections -> ConnectionsScreen(repository, keyStore)
                else -> {}
            }
        }
    }
}

@Composable
private fun LoginScreen(onLogin: () -> Unit, onApi: () -> Unit) {
    var user by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    Column(
        Modifier.fillMaxSize().background(Bg).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text("AbhiBoT", color = Text, fontSize = 34.sp, fontWeight = FontWeight.Bold)
            Text(" 7.1", color = Blue, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Text("ADVANCED VIRTUAL TRADING", color = Muted, fontSize = 10.sp)
        Spacer(Modifier.height(40.dp))
        Card(colors = CardDefaults.cardColors(Panel), shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(18.dp)) {
                Text("Welcome back", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Sign in to your paper-trading account", color = Muted, fontSize = 11.sp)
            }
        }
        Spacer(Modifier.height(22.dp))
        OutlinedTextField(user, { user = it }, label = { Text("Email / Username") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(pass, { pass = it }, label = { Text("Password") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(18.dp))
        Button(onClick = onLogin, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(16.dp)) {
            Text("LOGIN TO ABHIBOT", fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(14.dp))
        OutlinedButton(onClick = onApi, modifier = Modifier.fillMaxWidth().height(50.dp)) {
            Text("API / BROKER LOGIN")
        }
        Spacer(Modifier.height(28.dp))
        Text("Paper Trading is ON by default", color = Green, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Text("No real order is placed in this mode.", color = Muted, fontSize = 10.sp)
    }
}

@Composable
private fun HomeScreen(onConnections: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Bg).padding(20.dp)) {
        Header("AbhiBoT 7.1", "PAPER TRADING • NSE")
        Card(colors = CardDefaults.cardColors(Panel), shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(18.dp).fillMaxWidth()) {
                Text("Virtual Balance", color = Muted, fontSize = 12.sp)
                Text("₹1,00,000.00", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text("+₹2,840.50  +2.91% Today", color = Green, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(20.dp))
        Text("Market Overview", fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        listOf("NIFTY 50" to "24,836.20", "BANK NIFTY" to "56,412.45", "SENSEX" to "81,986.12").forEach {
            MarketRow(it.first, it.second, if (it.first == "SENSEX") "-0.18%" else "+0.72%")
        }
        Spacer(Modifier.height(18.dp))
        Button(onClick = onConnections, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Settings, null); Spacer(Modifier.width(8.dp)); Text("API & LIVE DATA CONNECTIONS")
        }
    }
}

@Composable
private fun MarketsScreen(repository: MarketRepository, keyStore: SecureApiKeyStore) {
    var symbol by remember { mutableStateOf("AAPL") }
    var quote by remember { mutableStateOf<Double?>(null) }
    var status by remember { mutableStateOf("Add your API key in Connections") }
    val scope = rememberCoroutineScope()
    Column(Modifier.fillMaxSize().background(Bg).padding(20.dp)) {
        Header("Markets", "LIVE / API DATA")
        OutlinedTextField(symbol, { symbol = it }, label = { Text("Provider symbol") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(10.dp))
        Button(onClick = {
            val key = keyStore.read()
            if (key.isNullOrBlank()) status = "API key not configured"
            else scope.launch {
                status = "Updating…"
                repository.getPrice(symbol.trim(), key).onSuccess {
                    quote = it.price; status = "Updated from Twelve Data"
                }.onFailure { status = it.message ?: "Update failed" }
            }
        }, modifier = Modifier.fillMaxWidth()) { Text("REFRESH LIVE PRICE") }
        Spacer(Modifier.height(18.dp))
        quote?.let {
            Card(colors = CardDefaults.cardColors(Panel), shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(18.dp)) {
                    Text(symbol.uppercase(), color = Muted, fontSize = 11.sp)
                    Text("%.2f".format(it), fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Text(status, color = Green, fontSize = 10.sp)
                }
            }
        } ?: Text(status, color = Muted, fontSize = 11.sp)
    }
}

@Composable
private fun TradeScreen() {
    Column(Modifier.fillMaxSize().background(Bg).padding(20.dp)) {
        Header("Start Trade", "PAPER ORDER")
        Card(colors = CardDefaults.cardColors(Panel), shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.padding(18.dp)) {
                Text("NIFTY 50", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("24,836.20", color = Muted, fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = {}, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Green)) { Text("BUY") }
            Button(onClick = {}, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Red)) { Text("SELL") }
        }
        Spacer(Modifier.height(14.dp))
        OutlinedTextField("50", {}, label = { Text("Quantity") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        OutlinedTextField("24,836.20", {}, label = { Text("Entry Price") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        OutlinedTextField("24,700", {}, label = { Text("Stop Loss") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        OutlinedTextField("25,108.60", {}, label = { Text("Target") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(18.dp))
        Button(onClick = {}, modifier = Modifier.fillMaxWidth().height(52.dp)) { Text("PLACE PAPER ORDER", fontWeight = FontWeight.Bold) }
    }
}

@Composable
private fun OrdersScreen() {
    Column(Modifier.fillMaxSize().background(Bg).padding(20.dp)) {
        Header("Orders & Positions", "PAPER TRADES")
        Text("Open Positions", fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Card(colors = CardDefaults.cardColors(Panel), shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.padding(18.dp)) {
                Text("NIFTY 50 • BUY", fontWeight = FontWeight.Bold)
                Text("Qty 50   Entry ₹24,780   LTP ₹24,836", color = Muted, fontSize = 11.sp)
                Text("+₹2,810.00", color = Green, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(20.dp))
        Text("Recent Orders", fontSize = 17.sp, fontWeight = FontWeight.Bold)
        listOf("BUY NIFTY 50", "SELL BANK NIFTY", "BUY RELIANCE", "SELL TCS").forEach {
            MarketRow(it, "FILLED", "")
        }
    }
}

@Composable
private fun ProfileScreen(onConnections: () -> Unit, onLogout: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Bg).padding(20.dp)) {
        Header("Profile", "PAPER ACCOUNT")
        Card(colors = CardDefaults.cardColors(Panel), shape = RoundedCornerShape(20.dp)) {
            Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(54.dp).background(Blue, CircleShape), contentAlignment = Alignment.Center) {
                    Text("A", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text("Abhishek", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Text("Virtual Trader", color = Muted, fontSize = 11.sp)
                    Text("₹1,00,000 balance", color = Green, fontSize = 10.sp)
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        Button(onClick = onConnections, modifier = Modifier.fillMaxWidth()) { Text("API & DATA CONNECTIONS") }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) { Text("LOG OUT") }
    }
}

@Composable
private fun ConnectionsScreen(repository: MarketRepository, keyStore: SecureApiKeyStore) {
    var key by remember { mutableStateOf(keyStore.read().orEmpty()) }
    var saved by remember { mutableStateOf(key.isNotBlank()) }
    Column(Modifier.fillMaxSize().background(Bg).padding(20.dp)) {
        Header("Connections", "LIVE MARKET DATA + BROKERS")
        Card(colors = CardDefaults.cardColors(Color(0xFF101D2D)), shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("● DATA STREAM", color = Blue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(if (saved) "Configured" else "Not connected", color = if (saved) Green else Muted, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Twelve Data REST + WebSocket", color = Muted, fontSize = 10.sp)
            }
        }
        Spacer(Modifier.height(18.dp))
        Text("Twelve Data API Key", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = key, onValueChange = { key = it },
            label = { Text("API Key") }, singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
        Button(onClick = { keyStore.save(key.trim()); saved = key.isNotBlank() }, modifier = Modifier.fillMaxWidth()) {
            Text("SAVE SECURELY")
        }
        Spacer(Modifier.height(18.dp))
        Text("Broker Connections", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        listOf("Zerodha / Kite", "Dhan", "Angel One").forEach {
            Card(colors = CardDefaults.cardColors(Panel), shape = RoundedCornerShape(14.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(it, fontWeight = FontWeight.Bold)
                        Text("Connect later • backend required", color = Muted, fontSize = 9.sp)
                    }
                    Text("CONNECT", color = Blue, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Text("Production note: broker secrets should be handled by a backend; do not commit keys to GitHub.", color = Muted, fontSize = 9.sp)
    }
}

@Composable private fun Header(title: String, subtitle: String) {
    Column(Modifier.padding(bottom = 18.dp)) {
        Text(title, fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable private fun MarketRow(name: String, value: String, change: String) {
    Card(colors = CardDefaults.cardColors(Panel), shape = RoundedCornerShape(14.dp), modifier = Modifier.padding(vertical = 4.dp)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(name, Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(value, color = Muted, fontSize = 11.sp)
            Spacer(Modifier.width(10.dp))
            Text(change, color = if (change.startsWith("-")) Red else Green, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}
