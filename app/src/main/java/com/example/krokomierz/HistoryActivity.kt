package com.example.krokomierz

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.krokomierz.ui.theme.BottomNavBar
import com.example.krokomierz.ui.theme.KrokomierzTheme
import com.example.krokomierz.ui.theme.NavItem
import com.example.krokomierz.util.PrefKeys
import com.example.krokomierz.util.ThemeController
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class HistoryActivity : ComponentActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var currentStepsState: MutableIntState
    private var historyMapState = mutableStateOf<Map<String, Int>>(emptyMap())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(0, 0)

        ThemeController.load(this)

        prefs = getSharedPreferences(PrefKeys.FILE, Context.MODE_PRIVATE)
        currentStepsState = mutableIntStateOf(prefs.getInt(PrefKeys.STEPS, 0))
        historyMapState.value = loadHistory(prefs)

        setContent {
            KrokomierzTheme(darkTheme = ThemeController.isDark.value) {
                HistoryScaffold {
                    HistoryScreen(
                        todaySteps = currentStepsState.intValue,
                        historyMap = historyMapState.value,
                        onSaveToday = ::saveTodayToHistory
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        currentStepsState.intValue = prefs.getInt(PrefKeys.STEPS, 0)
    }

    @Composable
    private fun HistoryScaffold(content: @Composable () -> Unit) {
        val ctx = LocalContext.current
        Scaffold(
            bottomBar = {
                BottomNavBar(
                    selected = NavItem.HISTORIA,
                    onKrokomierzClick = {
                        ctx.startActivity(
                            Intent(ctx, MainActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                        )
                        (ctx as Activity).overridePendingTransition(0, 0)
                    },
                    onHistoriaClick = { },
                    onProfilClick = {
                        ctx.startActivity(
                            Intent(ctx, ProfileActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                        )
                        (ctx as Activity).overridePendingTransition(0, 0)
                    }
                )
            }
        ) { padding -> Box(Modifier.padding(padding)) { content() } }
    }

    @Composable
    private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape  = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.06f)
            )
        ) { Column(Modifier.padding(20.dp), content = content) }
    }

    @Composable
    private fun HistoryScreen(
        todaySteps: Int,
        historyMap: Map<String, Int>,
        onSaveToday: () -> Unit
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SectionCard {
                Text("Dzisiaj: $todaySteps kroków",
                    style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))
                Button(onClick = onSaveToday) { Text("Zapisz dzisiejsze kroki") }
            }

            SectionCard {
                Text("Historia dni:", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))
                if (historyMap.isEmpty()) {
                    Text("Brak zapisów")
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(historyMap.toSortedMap(reverseOrder()).entries.toList()) { entry ->
                            Text("${entry.key} – ${entry.value} kroków",
                                modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }
            }
        }
    }
    
    private fun saveTodayToHistory() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val steps = currentStepsState.intValue
        val updated = historyMapState.value.toMutableMap().apply { this[today] = steps }
        historyMapState.value = updated
        saveHistory(prefs, updated)
    }
    private fun saveHistory(prefs: SharedPreferences, history: Map<String, Int>) {
        val json = JSONObject().apply { history.forEach { put(it.key, it.value) } }
        prefs.edit().putString(PrefKeys.HISTORY_JSON, json.toString()).apply()
    }
    private fun loadHistory(prefs: SharedPreferences): Map<String, Int> {
        val jsonString = prefs.getString(PrefKeys.HISTORY_JSON, "{}") ?: "{}"
        val obj = JSONObject(jsonString)
        return buildMap {
            val keys = obj.keys()
            while (keys.hasNext()) {
                val k = keys.next(); put(k, obj.optInt(k, 0))
            }
        }
    }
}
