package com.example.krokomierz

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.krokomierz.ui.theme.KrokomierzTheme
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import android.content.SharedPreferences


class HistoryActivity : ComponentActivity() {

    private val PREFS_NAME = "KrokomierzPrefs"
    private val KEY_HISTORY = "step_history"
    private val KEY_CURRENT_STEPS = "key_steps"

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
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val currentSteps = prefs.getInt(KEY_CURRENT_STEPS, 0)

        var historyMap by remember {
            mutableStateOf(loadHistory(prefs))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Dzisiaj: $currentSteps kroków", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = {
                historyMap = historyMap.toMutableMap().apply {
                    this[today] = currentSteps
                    saveHistory(prefs, this)
                }
            }) {
                Text("Zapisz dzisiejsze kroki")
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(text = "Historia dni:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(historyMap.toSortedMap(reverseOrder()).entries.toList()) { entry ->
                    Text("${entry.key} – ${entry.value} kroków", modifier = Modifier.padding(4.dp))
                }
            }
        }
    }

    private fun saveHistory(prefs: SharedPreferences, history: Map<String, Int>) {
        val json = JSONObject()
        for ((date, steps) in history) {
            json.put(date, steps)
        }
        prefs.edit().putString(KEY_HISTORY, json.toString()).apply()
    }

    private fun loadHistory(prefs: SharedPreferences): Map<String, Int> {
        val jsonString = prefs.getString(KEY_HISTORY, "{}") ?: "{}"
        val jsonObject = JSONObject(jsonString)
        val map = mutableMapOf<String, Int>()
        val keys = jsonObject.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            map[key] = jsonObject.optInt(key, 0)
        }
        return map
    }
}
