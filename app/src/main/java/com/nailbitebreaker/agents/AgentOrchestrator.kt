package com.nailbitebreaker.agents

import com.nailbitebreaker.data.HabitRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Central coordinator of the multi-agent system.
 */
class AgentOrchestrator(private val repository: HabitRepository) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _eventBus = MutableSharedFlow<AgentEvent>(extraBufferCapacity = 64)

    val eventBus: SharedFlow<AgentEvent> = _eventBus.asSharedFlow()

    // ── Agent instances ───────────────────────────────────────────────────────

    val detectionAgent = DetectionAgent(
        agentScope = scope,
        emit = { event -> _eventBus.tryEmit(event) }
    )

    val coachingAgent = CoachingAgent(
        agentScope = scope,
        eventBus = eventBus,
        emit = { event -> _eventBus.tryEmit(event) }
    )

    val progressAgent = ProgressAgent(
        agentScope = scope,
        eventBus = eventBus,
        repository = repository
    )

    val rewardAgent = RewardAgent(
        agentScope = scope,
        eventBus = eventBus
    )

    /**
     * Social Support Agent: Provides emotional validation and a friendly chat interface.
     */
    val socialSupportAgent = SocialSupportAgent(
        agentScope = scope,
        eventBus = eventBus
    )

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    fun start() {
        detectionAgent.start()
        coachingAgent.start()
        progressAgent.start()
        rewardAgent.start()
        socialSupportAgent.start()
    }

    fun stop() {
        detectionAgent.stop()
        coachingAgent.stop()
        progressAgent.stop()
        rewardAgent.stop()
        socialSupportAgent.stop()
        scope.cancel()
    }
}
