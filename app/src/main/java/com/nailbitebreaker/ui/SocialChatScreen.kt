package com.nailbitebreaker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nailbitebreaker.agents.ChatMessage
import com.nailbitebreaker.viewmodel.SocialSupportViewModel



/**
 * Social Chat Screen
 *
 * Interactive chat with the Social Support Agent.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialChatScreen(
    onBack: () -> Unit,
    viewModel: SocialSupportViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var inputText by remember {
        mutableStateOf("")
    }

    val listState = rememberLazyListState()

    /*
     * Automatically scroll to the newest message.
     *
     * We intentionally use scrollToItem() instead of
     * animateScrollToItem(). This avoids leaving an animation
     * coroutine running against the LazyColumn when the user
     * navigates away from this screen.
     */
    LaunchedEffect(
        state.messages.size,
        state.isTyping
    ) {
        val messageCount = state.messages.size

        if (messageCount > 0) {
            val targetIndex = if (state.isTyping) {
                // The typing indicator is added after the messages.
                messageCount
            } else {
                // Last actual message.
                messageCount - 1
            }

            if (targetIndex >= 0) {
                listState.scrollToItem(targetIndex)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.background
            )
    ) {

        // ---------------------------------------------------------
        // Header
        // ---------------------------------------------------------

        Surface(
            tonalElevation = 4.dp,
            shadowElevation = 4.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 8.dp,
                        vertical = 12.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconButton(
                    onClick = onBack
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }

                Spacer(
                    modifier = Modifier.width(8.dp)
                )

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🤝",
                        fontSize = 20.sp
                    )
                }

                Spacer(
                    modifier = Modifier.width(12.dp)
                )

                Column {
                    Text(
                        text = "Supportive Friend",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = if (state.isTyping) {
                            "Typing..."
                        } else {
                            "Online"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (state.isTyping) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            Color.Gray
                        }
                    )
                }
            }
        }

        // ---------------------------------------------------------
        // Chat Messages
        // ---------------------------------------------------------

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            items(
                items = state.messages,
                key = { message ->
                    /*
                     * If ChatMessage has a unique ID, use:
                     *
                     * key = { it.id }
                     *
                     * hashCode() is used here as a safe fallback
                     * with the current ChatMessage model.
                     */
                    message.hashCode()
                }
            ) { message ->

                ChatBubble(
                    message = message
                )
            }

            // Typing indicator
            if (state.isTyping) {
                item(
                    key = "typing_indicator"
                ) {
                    TypingIndicator()
                }
            }
        }

        // ---------------------------------------------------------
        // Input Area
        // ---------------------------------------------------------

        Surface(
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                OutlinedTextField(
                    value = inputText,
                    onValueChange = {
                        inputText = it
                    },
                    placeholder = {
                        Text("Talk to me...")
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor =
                            MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor =
                            MaterialTheme.colorScheme.outline
                    ),
                    maxLines = 3
                )

                Spacer(
                    modifier = Modifier.width(12.dp)
                )

                IconButton(
                    onClick = {
                        val message = inputText.trim()

                        if (message.isNotEmpty()) {
                            viewModel.sendMessage(message)
                            inputText = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

/**
 * Individual chat message bubble.
 */
@Composable
fun ChatBubble(
    message: ChatMessage
) {
    val alignment = if (message.isFromAgent) {
        Alignment.CenterStart
    } else {
        Alignment.CenterEnd
    }

    val containerColor = if (message.isFromAgent) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.primary
    }

    val contentColor = if (message.isFromAgent) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onPrimary
    }

    val shape = if (message.isFromAgent) {
        RoundedCornerShape(
            topStart = 4.dp,
            topEnd = 20.dp,
            bottomEnd = 20.dp,
            bottomStart = 20.dp
        )
    } else {
        RoundedCornerShape(
            topStart = 20.dp,
            topEnd = 20.dp,
            bottomEnd = 4.dp,
            bottomStart = 20.dp
        )
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Surface(
            color = containerColor,
            shape = shape,
            modifier = Modifier.widthIn(
                max = 280.dp
            )
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 10.dp
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = contentColor
            )
        }
    }
}

/**
 * Typing indicator displayed while the agent is responding.
 */
@Composable
fun TypingIndicator() {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer.copy(
            alpha = 0.5f
        ),
        shape = RoundedCornerShape(
            topStart = 4.dp,
            topEnd = 16.dp,
            bottomEnd = 16.dp,
            bottomStart = 16.dp
        ),
        modifier = Modifier.width(60.dp)
    ) {
        Text(
            text = "...",
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 4.dp
            ),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

