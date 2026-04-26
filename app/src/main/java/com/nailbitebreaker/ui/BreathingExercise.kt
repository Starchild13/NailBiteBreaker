package com.nailbitebreaker.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

/**
 * Animated circular breathing guide composable.
 *
 * Implements the 4-4-4-4 box-breathing pattern as a visual aid:
 *   Inhale (4 s) → Hold (4 s) → Exhale (4 s) → Hold (4 s)
 *
 * A single 0→1 progress value drives the full 16-second cycle (4 phases × 4 s).
 * A sine wave maps progress to a radius scale so the transitions feel organic
 * rather than mechanical. Three concentric circles create a depth effect:
 *   - outer ripple  (low opacity — suggests expansion)
 *   - mid ring      (medium opacity — the breath boundary)
 *   - inner core    (solid — the focal point for the user's gaze)
 *
 * @param modifier Optional modifier passed from the parent composable.
 */
@Composable
fun BreathingExercise(modifier: Modifier = Modifier) {
    // Drive the entire 16-second cycle with a single infinite transition.
    val infiniteTransition = rememberInfiniteTransition(label = "breathing_cycle")

    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16_000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "breath_progress"
    )

    // Map the linear 0-1 progress to a smooth 0-1 expansion scale using sine.
    // Offset by -PI/2 so the circle starts fully contracted (inhale from minimum).
    val scale = 0.5f + 0.5f * sin(progress * 2f * PI.toFloat() - PI.toFloat() / 2f)

    // Derive the phase label from which quarter of the cycle we're in.
    val phase = when {
        progress < 0.25f -> "Inhale"
        progress < 0.50f -> "Hold"
        progress < 0.75f -> "Exhale"
        else             -> "Hold"
    }

    val primary = MaterialTheme.colorScheme.primary

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(240.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val maxRadius = size.minDimension / 2f
                val currentRadius = maxRadius * scale

                // Outer ripple — subtle glow suggesting the breath boundary.
                drawCircle(
                    color = primary.copy(alpha = 0.12f),
                    radius = currentRadius * 1.35f
                )
                // Mid ring — the main animated breath circle.
                drawCircle(
                    color = primary.copy(alpha = 0.30f),
                    radius = currentRadius
                )
                // Inner core — solid focal point, always visible.
                drawCircle(
                    color = primary,
                    radius = currentRadius * 0.55f
                )
            }

            // Phase label overlaid on the inner core.
            Text(
                text = phase,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "4 s inhale  ·  4 s hold  ·  4 s exhale  ·  4 s hold",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
        )
    }
}
