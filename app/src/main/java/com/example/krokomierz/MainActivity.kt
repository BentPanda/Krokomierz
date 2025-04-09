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
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.krokomierz.ui.theme.KrokomierzTheme

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
                        val editor = prefs.edit()
                        editor.putInt(KEY_STEPS, currentSteps.value)
                        editor.apply()
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
            val totalStepsFromSensor = event.values[0]
            if (!isInitialSet) {
                initialSteps = totalStepsFromSensor
                isInitialSet = true
            }
            val newSteps = totalStepsFromSensor - initialSteps
            currentSteps.value = newSteps.toInt()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

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
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Kroki: $stepCount", modifier = Modifier.padding(8.dp))
        Spacer(modifier = Modifier.height(16.dp))

        val distance = stepCount * 0.75 // 0.75m na krok (przykład)
        val calories = stepCount * 0.04 // 0.04 kcal na krok (przykład)
        Text(text = "Dystans: %.2f m".format(distance))
        Text(text = "Kalorie: %.2f kcal".format(calories))
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onSaveSteps) {
            Text(text = "Zapisz stan")
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onGoToHistory) {
            Text(text = "Pokaż historię")
        }
    }
}
