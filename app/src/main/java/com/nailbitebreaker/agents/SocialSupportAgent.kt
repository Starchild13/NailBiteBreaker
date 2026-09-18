package com.nailbitebreaker.agents

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch

/**
 * A message in the social support chat.
 */
data class ChatMessage(
    val text: String,
    val isFromAgent: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * State for the Social Support Agent.
 */
data class SocialSupportState(
    val messages: List<ChatMessage> = emptyList(),
    val isTyping: Boolean = false
)

/**
 * Social Support Agent - Acts like a supportive friend to validate feelings
 * and provide emotional reinforcement during an urge.
 */
class SocialSupportAgent(
    private val agentScope: CoroutineScope,
    private val eventBus: SharedFlow<AgentEvent>
) : Agent {

    override val agentId: String = "social-support-agent"

    private val _state = MutableStateFlow(SocialSupportState())
    val state: StateFlow<SocialSupportState> = _state.asStateFlow()

    private var subscriptionJob: Job? = null

    private val supportResponses = listOf(
        "I hear you. It's totally okay to feel this way right now.",
        "That sounds really tough. Remember, you've handled this before and you can do it again.",
        "It's just a feeling, and feelings always pass. I'm right here with you.",
        "You're doing great just by acknowledging the urge. That's the biggest step!",
        "Take a deep breath. You are stronger than this habit.",
        "I'm proud of you for reaching out instead of giving in. You've got this!"
    )

    override fun start() {
        subscriptionJob = agentScope.launch {
            eventBus
                .filterIsInstance<AgentEvent.UrgeDetected>()
                .collect { event ->
                    onUrgeDetected(event.trigger)
                }
        }
    }

    override fun stop() {
        subscriptionJob?.cancel()
    }

    private fun onUrgeDetected(trigger: String) {
        agentScope.launch(Dispatchers.Default) {
            val initialMessage = ChatMessage(
                text = "Hey, I see you're feeling a bit triggered by $trigger. How are you holding up?",
                isFromAgent = true
            )
            _state.value = SocialSupportState(messages = listOf(initialMessage))
        }
    }

    /**
     * User sends a message to the agent.
     */
    fun sendMessage(text: String) {
        val userMsg = ChatMessage(text = text, isFromAgent = false)
        val currentMsgs = _state.value.messages + userMsg
        _state.value = _state.value.copy(messages = currentMsgs, isTyping = true)

        agentScope.launch(Dispatchers.Default) {
            // Simulate "thinking" time
            kotlinx.coroutines.delay(1500)
            val response = ChatMessage(text = supportResponses.random(), isFromAgent = true)
            _state.value = _state.value.copy(
                messages = _state.value.messages + response,
                isTyping = false
            )
        }
    }
}
