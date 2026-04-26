package com.nailbitebreaker.agents

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch

// ── Domain model ───────────────────────────────────────────────────────────────

/**
 * A single reward item shown when the user completes a coaching session.
 *
 * @param badge   Emoji displayed as the centrepiece of the reward overlay.
 * @param message Personalised congratulatory message.
 * @param milestone Optional milestone label shown for notable session counts (e.g. "5 sessions!").
 */
data class RewardInfo(
    val badge: String,
    val message: String,
    val milestone: String? = null
)

/**
 * Complete state of the Reward Agent exposed to the UI.
 *
 * @param isVisible     True when the reward overlay should be displayed.
 * @param reward        The selected reward for this session (null when not visible).
 * @param totalSessions Running count of completed sessions — used to derive milestones.
 */
data class RewardState(
    val isVisible: Boolean = false,
    val reward: RewardInfo? = null,
    val totalSessions: Int = 0
)

// ── Reward Agent ───────────────────────────────────────────────────────────────

/**
 * The Reward Agent listens for [AgentEvent.SessionCompleted] events and responds
 * by selecting an encouraging reward message to display to the user.
 *
 * Reward rotation: rewards cycle through [rewards] so the user sees variety.
 * Milestone rewards override the rotation at specific session counts (5, 10, 25, 50…)
 * to celebrate meaningful achievements with a special message.
 *
 * @param agentScope Coroutine scope shared across all agents (application lifetime).
 * @param eventBus   Read-only view of the orchestrator's event bus.
 */
class RewardAgent(
    private val agentScope: CoroutineScope,
    private val eventBus: SharedFlow<AgentEvent>
) : Agent {

    override val agentId: String = "reward-agent"

    /** Rotating pool of reward messages shown after each completed session. */
    private val rewards: List<RewardInfo> = listOf(
        RewardInfo("🌟", "Incredible! You resisted the urge — that's real strength!"),
        RewardInfo("💪", "Willpower champion! Your nails thank you."),
        RewardInfo("🎉", "Yes! Every resistance makes the next one even easier."),
        RewardInfo("🦁", "Fearless! Anxiety tried, but YOU won."),
        RewardInfo("🌈", "Beautiful self-control. Be proud of this moment!"),
        RewardInfo("🚀", "Habit broken, one session at a time. Keep soaring!"),
        RewardInfo("🧘", "Mind over habit. You're proving it works."),
        RewardInfo("✨", "That uncomfortable urge passed — it always does."),
        RewardInfo("🏅", "Session complete! You're building an amazing habit.")
    )

    /** Milestone overrides — shown at notable session counts instead of rotating rewards. */
    private val milestones: Map<Int, RewardInfo> = mapOf(
        5  to RewardInfo("🎖️", "5 sessions completed! You're on a roll!", "5 Sessions!"),
        10 to RewardInfo("🔟", "10 coaching sessions! The habit is weakening.", "10 Sessions!"),
        25 to RewardInfo("🏆", "25 sessions! You are a habit-breaking machine.", "25 Sessions!"),
        50 to RewardInfo("👑", "50 sessions! You've truly transformed your habit.", "50 Sessions!")
    )

    private var rewardIndex = 0
    private var subscriptionJob: Job? = null

    private val _state = MutableStateFlow(RewardState())

    /** Read-only state collected by [CoachViewModel] and displayed on [CoachScreen]. */
    val state: StateFlow<RewardState> = _state.asStateFlow()

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    override fun start() {
        subscriptionJob = agentScope.launch {
            eventBus
                .filterIsInstance<AgentEvent.SessionCompleted>()
                .collect { event -> onSessionCompleted(event) }
        }
    }

    override fun stop() {
        subscriptionJob?.cancel()
        subscriptionJob = null
    }

    // ── Event handling ────────────────────────────────────────────────────────

    /**
     * Selects a reward (milestone override or next in rotation) and marks
     * the overlay as visible.
     *
     * @param event The [AgentEvent.SessionCompleted] fired by [CoachingAgent].
     */
    private fun onSessionCompleted(event: AgentEvent.SessionCompleted) {
        // Check for a milestone reward first; fall back to rotating pool.
        val reward = milestones[event.totalSessions]
            ?: rewards[rewardIndex++ % rewards.size]

        _state.value = RewardState(
            isVisible = true,
            reward = reward,
            totalSessions = event.totalSessions
        )
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Hides the reward overlay.
     * Called by [CoachViewModel.dismissReward] when the user taps "Continue".
     */
    fun dismissReward() {
        _state.value = _state.value.copy(isVisible = false)
    }
}
