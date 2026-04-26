package com.nailbitebreaker.agents

import kotlinx.coroutines.CoroutineScope

/**
 * The Detection Agent is the entry point for urge events in the multi-agent system.
 *
 * Responsibility: accept user-triggered urge signals from the UI and publish
 * them onto the shared [AgentOrchestrator] event bus so that [CoachingAgent]
 * and [ProgressAgent] can react.
 *
 * In this release, detection is user-driven (panic button tap). A future
 * version could wire in an accelerometer gesture recogniser here without
 * changing any other agent or UI code.
 *
 * @param agentScope  The coroutine scope shared across all agents (application lifetime).
 * @param emit        Callback that posts an [AgentEvent] to the orchestrator's event bus.
 */
class DetectionAgent(
    @Suppress("unused") private val agentScope: CoroutineScope,
    private val emit: (AgentEvent) -> Unit
) : Agent {

    override val agentId: String = "detection-agent"

    // No background subscription needed; events are purely user-driven.
    override fun start() = Unit
    override fun stop() = Unit

    /**
     * Called by the UI (via [HomeViewModel]) when the user taps the urge button.
     *
     * Creates an [AgentEvent.UrgeDetected] with the current timestamp and
     * the user-selected trigger context, then emits it to the event bus.
     *
     * @param trigger Short label describing what triggered the urge
     *                (e.g. "Work Stress", "Boredom", "Watching TV").
     */
    fun reportUrge(trigger: String) {
        val event = AgentEvent.UrgeDetected(trigger = trigger)
        emit(event)
    }
}
