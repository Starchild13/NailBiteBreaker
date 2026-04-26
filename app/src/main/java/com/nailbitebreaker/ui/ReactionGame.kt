package com.nailbitebreaker.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

/** Route constant for navigation. */
const val REACTION_GAME_ROUTE = "reaction_game"

/**
 * Data class representing a target circle in the reaction game.
 */
data class TargetCircle(
    val id: Long,
    val xPercent: Float,
    val yPercent: Float,
    val radius: Float,
    val color: Color,
    val isGood: Boolean,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Reaction Tap Game - A distraction exercise to engage the brain and hands.
 */
@Composable
fun ReactionGame(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var score by remember { mutableStateOf(0) }
    var gameState by remember { mutableStateOf(GameState.IDLE) }
    val targets = remember { mutableStateListOf<TargetCircle>() }

    // Game Loop
    LaunchedEffect(gameState) {
        if (gameState == GameState.PLAYING) {
            score = 0
            targets.clear()
            
            while (gameState == GameState.PLAYING) {
                delay(calculateSpawnDelay(score))
                
                // Spawn a new target
                val isGood = Random.nextFloat() > 0.2f // 80% good targets
                val newTarget = TargetCircle(
                    id = System.currentTimeMillis(),
                    xPercent = Random.nextFloat() * 0.8f + 0.1f,
                    yPercent = Random.nextFloat() * 0.8f + 0.1f,
                    radius = Random.nextFloat() * 20f + 30f,
                    color = if (isGood) Color(0xFF4CAF50) else Color(0xFFF44336),
                    isGood = isGood
                )
                targets.add(newTarget)
                
                // Remove old targets
                val now = System.currentTimeMillis()
                targets.removeAll { now - it.createdAt > 2000 }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Reaction Tap",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Score: $score",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Game Area
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    RoundedCornerShape(24.dp)
                )
                .pointerInput(gameState) {
                    if (gameState == GameState.PLAYING) {
                        detectTapGestures { offset ->
                            val tappedTarget = targets.find { target ->
                                val targetX = target.xPercent * size.width
                                val targetY = target.yPercent * size.height
                                val distance = Math.sqrt(
                                    Math.pow((offset.x - targetX).toDouble(), 2.0) +
                                            Math.pow((offset.y - targetY).toDouble(), 2.0)
                                ).toFloat()
                                distance <= target.radius * 2.5f // Give some leniency for touch
                            }

                            if (tappedTarget != null) {
                                if (tappedTarget.isGood) {
                                    score += 10
                                } else {
                                    score = (score - 15).coerceAtLeast(0)
                                }
                                targets.remove(tappedTarget)
                            }
                        }
                    }
                }
        ) {
            if (gameState == GameState.IDLE) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "🎯", fontSize = 64.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Tap the Green circles!\nAvoid the Red ones.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { gameState = GameState.PLAYING },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Start Game", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    targets.forEach { target ->
                        drawCircle(
                            color = target.color,
                            radius = target.radius.dp.toPx(),
                            center = Offset(
                                target.xPercent * size.width,
                                target.yPercent * size.height
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        if (gameState == GameState.PLAYING) {
            Button(
                onClick = { gameState = GameState.IDLE },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Stop Game")
            }
        }
    }
}

private fun calculateSpawnDelay(score: Int): Long {
    return (800 - (score / 50 * 50)).toLong().coerceAtLeast(350L)
}

enum class GameState { IDLE, PLAYING }
