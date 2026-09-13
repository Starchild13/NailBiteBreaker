package com.nailbitebreaker.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.nailbitebreaker.ui.theme.CoralButton
import com.nailbitebreaker.ui.theme.Lavender
import com.nailbitebreaker.ui.theme.TealAccent
import com.nailbitebreaker.viewmodel.HomeViewModel

/**
 * Predefined trigger-context labels shown as selectable filter chips.
 * The user picks the best match before tapping the urge button so
 * [ProgressAgent] can log richer context data.
 */
private val TRIGGERS = listOf(
    "Work Stress", "Boredom", "Watching TV",
    "Anxious", "Tired", "Other"
)

/**
 * Home screen — the main dashboard of NailBiteBreaker.
 *
 * Layout (top to bottom):
 *   1. App title + motivational subtitle
 *   2. Streak + today's count summary card
 *   3. Trigger context selector (filter chips)
 *   4. Pulsing "I Feel the Urge!" panic button
 *   5. Quick-access "Try Breathing" button
 *
 * Tapping the panic button calls [HomeViewModel.reportUrge], which fires an
 * [AgentEvent.UrgeDetected] event that immediately activates [CoachingAgent]
 * and persists the event via [ProgressAgent].
 *
 * @param navController Routes the user to coaching or breathing screens.
 * @param viewModel     Injected automatically by Compose's ViewModel factory.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel()
) {
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    var selectedTrigger by remember { mutableStateOf(TRIGGERS[0]) }
    var showFeedback by remember { mutableStateOf(false) }

    // Pulse animation for the urge button — subtle breathing effect.
    val pulse = rememberInfiniteTransition(label = "urge_pulse")
    val pulseScale by pulse.animateFloat(
        initialValue = 1f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // ── Title ─────────────────────────────────────────────────────────────
        Text(
            text = "NailBiteBreaker",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Your habit coach is ready 💪",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ── Streak & today's count card ───────────────────────────────────────
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "🔥", fontSize = 36.sp)
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "${stats.currentStreak} day streak",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Best: ${stats.longestStreak} days  ·  Today: ${stats.todayCount} urges",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Trigger selector ──────────────────────────────────────────────────
        Text(
            text = "What's triggering you?",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            TRIGGERS.forEach { trigger ->
                FilterChip(
                    selected = trigger == selectedTrigger,
                    onClick = { selectedTrigger = trigger },
                    label = { Text(trigger) }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ── Panic button ──────────────────────────────────────────────────────
        Box(contentAlignment = Alignment.Center) {
            // Outer glow ring — visible as a soft halo behind the button.
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .scale(pulseScale)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                CoralButton.copy(alpha = 0.20f),
                                CoralButton.copy(alpha = 0f)
                            )
                        ),
                        shape = CircleShape
                    )
            )

            // The main button.
            Button(
                onClick = {
                    viewModel.reportUrge(selectedTrigger)
                    showFeedback = true
                    navController.navigate(Screen.Coach.route)
                },
                modifier = Modifier
                    .size(148.dp)
                    .scale(pulseScale)
                    .shadow(12.dp, CircleShape),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = CoralButton)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "😰", fontSize = 28.sp)
                    Text(
                        text = "I Feel\nthe Urge!",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onError,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Feedback message after tapping ────────────────────────────────────
        AnimatedVisibility(
            visible = showFeedback,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut()
        ) {
            Text(
                text = "✅ Urge logged! Opening coach…",
                style = MaterialTheme.typography.bodyMedium,
                color = TealAccent,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── Quick breathing shortcut ──────────────────────────────────────────
        OutlinedButton(
            onClick = { navController.navigate(Screen.Breathe.route) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = "🌬️  Try Breathing Exercise",
                style = MaterialTheme.typography.labelLarge,
                color = Lavender
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── Tip Jar / RevenueCat integration ─────────────────────────────────
        Text(
            text = "Support the App",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.clickable { navController.navigate(Screen.Paywall.route) },
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}
