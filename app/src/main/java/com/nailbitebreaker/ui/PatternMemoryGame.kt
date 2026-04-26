package com.nailbitebreaker.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

/** Route constant for navigation. */
const val PATTERN_GAME_ROUTE = "pattern_game"

/**
 * Pattern Memory Game - A "Simon Says" style exercise to engage brain and hands.
 */
@Composable
fun PatternMemoryGame(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sequence = remember { mutableStateListOf<Int>() }
    val userSequence = remember { mutableStateListOf<Int>() }
    
    var gameState by remember { mutableStateOf(PatternGameState.IDLE) }
    var activeButton by remember { mutableStateOf(-1) }
    var feedbackText by remember { mutableStateOf("Watch the pattern...") }

    val colors = listOf(
        Color(0xFF4CAF50), // Green
        Color(0xFF2196F3), // Blue
        Color(0xFFFFEB3B), // Yellow
        Color(0xFFF44336)  // Red
    )

    LaunchedEffect(gameState) {
        if (gameState == PatternGameState.SHOWING_SEQUENCE) {
            feedbackText = "Watch carefully!"
            delay(1000)
            for (index in sequence) {
                activeButton = index
                delay(600)
                activeButton = -1
                delay(200)
            }
            userSequence.clear()
            gameState = PatternGameState.USER_INPUT
            feedbackText = "Your turn! Repeat the pattern."
        }
    }

    fun startNewRound() {
        sequence.add(Random.nextInt(4))
        userSequence.clear()
        gameState = PatternGameState.SHOWING_SEQUENCE
    }

    fun onButtonClicked(index: Int) {
        if (gameState != PatternGameState.USER_INPUT) return

        userSequence.add(index)
        val currentStep = userSequence.size - 1
        
        if (userSequence[currentStep] == sequence[currentStep]) {
            if (userSequence.size == sequence.size) {
                feedbackText = "Correct! Level Up!"
                gameState = PatternGameState.IDLE
            }
        } else {
            feedbackText = "Wrong! Try Level 1 again."
            sequence.clear()
            gameState = PatternGameState.IDLE
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val isWide = maxWidth > 600.dp
        val horizontalPadding = if (isWide) (maxWidth - 600.dp) / 2 else 24.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = horizontalPadding, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically)
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
                    text = "Pattern Memory",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Level: ${sequence.size}",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = feedbackText,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            // Adaptive Game Grid
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .widthIn(max = 500.dp)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(32.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        MemoryButton(colors[0], activeButton == 0, Modifier.weight(1f)) { onButtonClicked(0) }
                        MemoryButton(colors[1], activeButton == 1, Modifier.weight(1f)) { onButtonClicked(1) }
                    }
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        MemoryButton(colors[2], activeButton == 2, Modifier.weight(1f)) { onButtonClicked(2) }
                        MemoryButton(colors[3], activeButton == 3, Modifier.weight(1f)) { onButtonClicked(3) }
                    }
                }
            }

            if (gameState == PatternGameState.IDLE) {
                Button(
                    onClick = { startNewRound() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .widthIn(max = 400.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = if (sequence.isEmpty()) "Start Training" else "Next Level",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                // Spacer to maintain height when button is hidden
                Spacer(modifier = Modifier.height(56.dp))
            }
        }
    }
}

@Composable
fun MemoryButton(
    baseColor: Color,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val color by animateColorAsState(
        targetValue = if (isActive) Color.White else baseColor,
        animationSpec = tween(durationMillis = 200),
        label = "button_flash"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(color)
            .clickable { onClick() }
    )
}

enum class PatternGameState { IDLE, SHOWING_SEQUENCE, USER_INPUT }
