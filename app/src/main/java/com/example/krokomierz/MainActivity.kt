package com.example.krokomierz

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.krokomierz.ui.theme.BottomNavBar
import com.example.krokomierz.ui.theme.KrokomierzTheme
import com.example.krokomierz.ui.theme.NavItem
import com.example.krokomierz.util.PrefKeys
import kotlin.math.roundToInt
import kotlin.math.roundToLong

class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null

    private lateinit var prefs: SharedPreferences

    private var currentSteps  = mutableIntStateOf(0)
    private var dailyGoal     = mutableIntStateOf(0)
    private var counterOffset = Float.NaN

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(0, 0)

        requestActivityRecognition()

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepSensor    = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        prefs                = getSharedPreferences(PrefKeys.FILE, Context.MODE_PRIVATE)
        currentSteps.intValue = prefs.getInt(PrefKeys.STEPS, 0)
        dailyGoal.intValue    = prefs.getInt(PrefKeys.DAILY_GOAL, 0)
        counterOffset         = prefs.getFloat(PrefKeys.OFFSET_FLOAT, Float.NaN)

        setContent {
            KrokomierzTheme {
                MainScaffold(
                    stepCount    = currentSteps.intValue,
                    dailyGoal    = dailyGoal.intValue,
                    onGoalChange = ::updateDailyGoal
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        stepSensor?.also { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
    }
    override fun onPause() {
        super.onPause(); sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_STEP_COUNTER) return

        val total = event.values[0]
        if (counterOffset.isNaN()) {
            counterOffset = total - currentSteps.intValue
            prefs.edit().putFloat(PrefKeys.OFFSET_FLOAT, counterOffset).apply()
        }
        val fresh = (total - counterOffset).roundToLong().toInt()
        currentSteps.intValue = fresh
        prefs.edit().putInt(PrefKeys.STEPS, fresh).apply()
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun updateDailyGoal(g: Int) {
        dailyGoal.intValue = g
        prefs.edit().putInt(PrefKeys.DAILY_GOAL, g).apply()
    }
    private fun requestActivityRecognition() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACTIVITY_RECOGNITION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                1001
            )
        }
    }
}

@Composable
private fun MainScaffold(
    stepCount: Int,
    dailyGoal: Int,
    onGoalChange: (Int) -> Unit
) {
    val ctx = LocalContext.current
    Scaffold(
        bottomBar = {
            BottomNavBar(
                selected = NavItem.KROKI,
                onKrokomierzClick = {},
                onHistoriaClick = {
                    ctx.startActivity(
                        Intent(ctx, HistoryActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    )
                    (ctx as Activity).overridePendingTransition(0, 0)
                },
                onProfilClick = {
                    ctx.startActivity(
                        Intent(ctx, ProfileActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    )
                    (ctx as Activity).overridePendingTransition(0, 0)
                }
            )
        }
    ) { padding -> Box(Modifier.padding(padding)) {
        MainScreen(stepCount, dailyGoal, onGoalChange)
    }
    }
}

@Composable
fun MainScreen(
    stepCount: Int,
    dailyGoal: Int,
    onGoalChange: (Int) -> Unit
) {
    var goalInput by remember { mutableStateOf(if (dailyGoal > 0) dailyGoal.toString() else "") }
    val progress        = if (dailyGoal > 0) stepCount.toFloat() / dailyGoal else 0f
    val progressPercent = (progress * 100).coerceAtMost(100f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Kroki: $stepCount", modifier = Modifier.padding(bottom = 8.dp))
        Text("Dystans: %.2f m".format(stepCount * 0.75))
        Text("Kalorie: %.2f kcal".format(stepCount * 0.04))
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = goalInput,
            onValueChange = { goalInput = it },
            label = { Text("Cel kroków") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { onGoalChange(goalInput.toIntOrNull() ?: 0) },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Ustaw cel") }

        Spacer(Modifier.height(16.dp))
        if (dailyGoal > 0) {
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text("Postęp: ${progressPercent.roundToInt()} %")
        } else Text("Nie ustawiono celu")
    }
}
