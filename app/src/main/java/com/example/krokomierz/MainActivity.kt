package com.example.krokomierz

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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
import com.example.krokomierz.ui.theme.KrokomierzTheme
import kotlin.math.roundToInt

class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null

    private var initialSteps = 0f
    private var isInitialSet = false

    private var currentSteps = mutableStateOf(0)

    private val PREFS_NAME = "KrokomierzPrefs"
    private val KEY_STEPS = "key_steps"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkActivityRecognitionPermission()

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedSteps = prefs.getInt(KEY_STEPS, 0)
        currentSteps.value = savedSteps

        setContent {
            KrokomierzTheme {
                MainScreen(
                    stepCount = currentSteps.value,
                    onGoToHistory = {
                        startActivity(Intent(this, HistoryActivity::class.java))
                    },
                    onSaveSteps = {
                        prefs.edit().putInt(KEY_STEPS, currentSteps.value).apply()
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        stepSensor?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val total = event.values[0]
            if (!isInitialSet) {
                initialSteps = total
                isInitialSet = true
            }
            currentSteps.value = (total - initialSteps).toInt()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun checkActivityRecognitionPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
            != PackageManager.PERMISSION_GRANTED
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
fun MainScreen(
    stepCount: Int,
    onGoToHistory: () -> Unit,
    onSaveSteps: () -> Unit
) {
    var goalInput by remember { mutableStateOf("") }
    var dailyGoal by remember { mutableStateOf(0) }

    val progress = if (dailyGoal > 0) stepCount.toFloat() / dailyGoal.toFloat() else 0f
    val progressPercent = (progress * 100).coerceAtMost(100f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Kroki: $stepCount", modifier = Modifier.padding(bottom = 8.dp))

        val distance = stepCount * 0.75
        val calories = stepCount * 0.04

        Text("Dystans: %.2f m".format(distance))
        Text("Kalorie: %.2f kcal".format(calories))
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = goalInput,
            onValueChange = { goalInput = it },
            label = { Text("Cel kroków (np. 5000)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            dailyGoal = goalInput.toIntOrNull() ?: 0
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Ustaw cel")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (dailyGoal > 0) {
            LinearProgressIndicator(
                progress = progress.coerceIn(0f, 1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Postęp: ${progressPercent.roundToInt()} %")
        } else {
            Text("Nie ustawiono celu")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onSaveSteps, modifier = Modifier.fillMaxWidth()) {
            Text("Zapisz stan")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = onGoToHistory, modifier = Modifier.fillMaxWidth()) {
            Text("Pokaż historię")
        }
    }
}
