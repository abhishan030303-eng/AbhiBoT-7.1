package com.abhibot.sevenone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import com.abhibot.sevenone.ui.AbhiBoTApp
import com.abhibot.sevenone.ui.AbhiBoTTheme
import com.abhibot.sevenone.data.MarketRepository
import com.abhibot.sevenone.data.SecureApiKeyStore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val keyStore = SecureApiKeyStore(this)
        val repository = MarketRepository()
        setContent {
            AbhiBoTTheme {
                AbhiBoTApp(repository = repository, keyStore = keyStore)
            }
        }
    }
}
