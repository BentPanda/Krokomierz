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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.krokomierz.ui.theme.*
import com.example.krokomierz.util.PrefKeys
import com.example.krokomierz.util.ThemeController
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

        ThemeController.load(this)
        askActivityRecognition()

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepSensor    = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        prefs                = getSharedPreferences(PrefKeys.FILE, Context.MODE_PRIVATE)
        currentSteps.intValue = prefs.getInt(PrefKeys.STEPS, 0)
        dailyGoal.intValue    = prefs.getInt(PrefKeys.DAILY_GOAL, 0)
        counterOffset         = prefs.getFloat(PrefKeys.OFFSET_FLOAT, Float.NaN)

        setContent {
            KrokomierzTheme(darkTheme = ThemeController.isDark.value) {
                MainScaffold(
                    steps       = currentSteps.intValue,
                    dailyGoal   = dailyGoal.intValue,
                    onGoalChange = ::updateDailyGoal
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        stepSensor?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
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

    private fun updateDailyGoal(goal: Int) {
        dailyGoal.intValue = goal
        prefs.edit().putInt(PrefKeys.DAILY_GOAL, goal).apply()
    }
    private fun askActivityRecognition() {
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
    steps: Int,
    dailyGoal: Int,
    onGoalChange: (Int) -> Unit
) {
    val ctx = LocalContext.current
    Scaffold(
        bottomBar = {
            BottomNavBar(
                selected = NavItem.KROKI,
                onKrokomierzClick = { },
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
        MainScreen(steps, dailyGoal, onGoalChange)
    }}
}

@Composable
private fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape  = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor =
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.06f)
        )
    ) { Column(Modifier.padding(20.dp), content = content) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    steps: Int,
    dailyGoal: Int,
    onGoalChange: (Int) -> Unit
) {
    var goalInput by remember { mutableStateOf(if (dailyGoal > 0) dailyGoal.toString() else "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        SectionCard {
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
        }

        Spacer(Modifier.height(8.dp))

        SectionCard {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                StepRingProgress(steps = steps, goal = dailyGoal)
            }
        }

        Spacer(Modifier.height(8.dp))

        SectionCard {
            Text("Kalorie: %.2f kcal".format(steps * 0.04))
        }
    }
}
