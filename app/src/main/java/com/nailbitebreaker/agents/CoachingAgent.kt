package com.nailbitebreaker.agents

import com.nailbitebreaker.ui.PATTERN_GAME_ROUTE
import com.nailbitebreaker.ui.REACTION_GAME_ROUTE
import com.nailbitebreaker.ui.SOCIAL_CHAT_ROUTE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch

// ── Domain models exposed by this agent ────────────────────────────────────────

/** Classifies a coping technique so the UI can render it with the right icon/colour. */
enum class TechniqueType { BREATHING, GROUNDING, AFFIRMATION, DISTRACTION }

/**
 * A single coping technique recommended during a coaching session.
 *
 * @param title       Short headline shown on the coaching card.
 * @param description Step-by-step instructions for the user.
 * @param type        Category that controls the card icon and accent colour.
 * @param actionRoute Optional navigation route for an interactive exercise/game.
 */
data class CoachingTechnique(
    val title: String,
    val description: String,
    val type: TechniqueType,
    val actionRoute: String? = null
)

/**
 * The complete state snapshot of the Coaching Agent, exposed to the UI.
 *
 * @param technique    Currently recommended technique (null when no session is active).
 * @param isActive     True while a coaching session is open.
 * @param sessionCount Running count of sessions since app launch.
 */
data class CoachingState(
    val technique: CoachingTechnique? = null,
    val isActive: Boolean = false,
    val sessionCount: Int = 0
)

// ── Coaching Agent ─────────────────────────────────────────────────────────────

/**
 * The Coaching Agent listens for [AgentEvent.UrgeDetected] events emitted by
 * [DetectionAgent] and responds by cycling through evidence-based coping techniques.
 */
class CoachingAgent(
    private val agentScope: CoroutineScope,
    private val eventBus: SharedFlow<AgentEvent>,
    private val emit: (AgentEvent) -> Unit
) : Agent {

    override val agentId: String = "coaching-agent"

    // ── Technique catalogue ───────────────────────────────────────────────────

    private val techniques: List<CoachingTechnique> = listOf(
        CoachingTechnique(
            title = "Chat with a Friend",
            description = "You don't have to do this alone. Chat with your supportive AI friend to validate your feelings and get reinforcement to stay strong.",
            type = TechniqueType.AFFIRMATION,
            actionRoute = SOCIAL_CHAT_ROUTE
        ),
        CoachingTechnique(
            title = "Reaction Tap Game",
            description = "Engage your brain and hands immediately! Tap the green circles appearing randomly on the screen and avoid the red ones. This rapid task overrides the habit loop.",
            type = TechniqueType.DISTRACTION,
            actionRoute = REACTION_GAME_ROUTE
        ),
        CoachingTechnique(
            title = "Pattern Memory",
            description = "A \"Simon Says\" style game for your fingers. Watch the pattern and repeat it. This cognitive task requires focus, making it a powerful tool against urges.",
            type = TechniqueType.DISTRACTION,
            actionRoute = PATTERN_GAME_ROUTE
        ),
        CoachingTechnique(
            title = "Box Breathing",
            description = "Inhale for 4 s → Hold for 4 s → Exhale for 4 s → Hold for 4 s. " +
                "Repeat 4 times and feel your nervous system calm down.",
            type = TechniqueType.BREATHING
        ),
        CoachingTechnique(
            title = "5-4-3-2-1 Grounding",
            description = "Name 5 things you SEE · 4 you can TOUCH · 3 you HEAR · " +
                "2 you SMELL · 1 you TASTE. Anchors you to the present moment.",
            type = TechniqueType.GROUNDING
        ),
        CoachingTechnique(
            title = "Finger Tapping",
            description = "Touch each finger to your thumb one by one — index, middle, ring, " +
                "little — and count to 10 out loud. Gives your hands a healthy job to do!",
            type = TechniqueType.DISTRACTION
        ),
        CoachingTechnique(
            title = "Power Affirmation",
            description = "Repeat slowly three times: \"I am in control of my habits. " +
                "Every moment I resist builds my strength and confidence.\"",
            type = TechniqueType.AFFIRMATION
        ),
        CoachingTechnique(
            title = "Progressive Muscle Relaxation",
            description = "Clench both fists as hard as you can for 5 s, then release. " +
                "Repeat for your shoulders and jaw. Feel the tension dissolve.",
            type = TechniqueType.GROUNDING
        ),
        CoachingTechnique(
            title = "Cold Water Reset",
            description = "Run cold water over your wrists for 30 s. The temperature " +
                "activates the dive reflex, slowing your heart rate and reducing anxiety fast.",
            type = TechniqueType.DISTRACTION
        ),
        CoachingTechnique(
            title = "Mindful Observation",
            description = "Pick any object nearby. Spend 60 s studying its colour, " +
                "texture, weight, and any subtle details. Let it fill your entire attention.",
            type = TechniqueType.GROUNDING
        )
    )

    // ── State ─────────────────────────────────────────────────────────────────

    private var techniqueIndex = 0
    private var subscriptionJob: Job? = null
    private val _state = MutableStateFlow(CoachingState())
    val state: StateFlow<CoachingState> = _state.asStateFlow()

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    override fun start() {
        subscriptionJob = agentScope.launch {
            eventBus
                .filterIsInstance<AgentEvent.UrgeDetected>()
                .collect { onUrgeDetected() }
        }
    }

    override fun stop() {
        subscriptionJob?.cancel()
        subscriptionJob = null
    }

    // ── Event handling ────────────────────────────────────────────────────────

    private fun onUrgeDetected() {
        val technique = techniques[techniqueIndex % techniques.size]
        techniqueIndex++
        _state.value = CoachingState(
            technique = technique,
            isActive = true,
            sessionCount = _state.value.sessionCount + 1
        )
    }

    // ── Public API ──────────────────────────────────────────────────────────

    fun nextTechnique() {
        val technique = techniques[techniqueIndex % techniques.size]
        techniqueIndex++
        _state.value = _state.value.copy(technique = technique)
    }

    fun dismissSession() {
        val completedCount = _state.value.sessionCount
        _state.value = _state.value.copy(isActive = false)
        emit(AgentEvent.SessionCompleted(totalSessions = completedCount))
    }
}
