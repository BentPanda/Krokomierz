package com.example.krokomierz

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.krokomierz.ui.theme.KrokomierzTheme

class HistoryActivity : ComponentActivity() {

    private val PREFS_NAME = "KrokomierzPrefs"
    private val KEY_STEPS = "key_steps"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KrokomierzTheme {
                HistoryScreen()
            }
        }
    }

    @Composable
    fun HistoryScreen() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastSavedSteps = prefs.getInt(KEY_STEPS, 0)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Historia krokomierza (przykład)")
            Text(text = "Ostatnio zapisane kroki: $lastSavedSteps")
        }
    }
}
