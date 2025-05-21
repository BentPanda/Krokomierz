package com.example.krokomierz.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.min

/**
 * Okrągły pasek postępu z liczbą kroków w środku.
 *
 * @param steps  aktualna liczba kroków
 * @param goal   cel kroków (jeśli 0 – ring pokazuje jedynie tło)
 * @param ringSizeDp średnica okręgu
 * @param strokeWidth szerokość obrysu
 */
@Composable
fun StepRingProgress(
    steps: Int,
    goal: Int,
    modifier: Modifier = Modifier,
    ringSizeDp: Dp = 260.dp,
    strokeWidth: Dp = 24.dp
) {
    val density = LocalDensity.current
    val ringSizePx = with(density) { ringSizeDp.toPx() }
    val strokePx   = with(density) { strokeWidth.toPx() }

    val bgColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    val fgColor = MaterialTheme.colorScheme.primary
    val progress = if (goal > 0) steps.toFloat() / goal else 0f

    Column(
        modifier = modifier.width(ringSizeDp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(ringSizeDp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val diameter = min(ringSizePx, ringSizePx)
                val topLeft  = Offset(
                    (size.width - diameter) / 2f + strokePx / 2f,
                    (size.height - diameter) / 2f + strokePx / 2f
                )
                val arcSize = Size(diameter - strokePx, diameter - strokePx)

                // tło
                drawArc(
                    color = bgColor,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
                // postęp
                drawArc(
                    color = fgColor,
                    startAngle = -90f,
                    sweepAngle = progress.coerceIn(0f, 1f) * 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }

            Text(
                text = steps.toString(),
                style = MaterialTheme.typography.displayMedium
            )
        }

        Spacer(Modifier.height(8.dp))
        Text("Kroki", style = MaterialTheme.typography.titleMedium)
    }
}
