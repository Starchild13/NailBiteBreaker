package com.nailbitebreaker.agents

/**
 * Sealed hierarchy of all events that can travel through the inter-agent
 * event bus ([AgentOrchestrator.eventBus]).
 *
 * Using a sealed class ensures the compiler forces exhaustive when-expressions
 * at every event-handling site, making it impossible to forget a new event type.
 */
sealed class AgentEvent {

    /**
     * Emitted by [DetectionAgent] whenever the user reports a nail-biting urge.
     *
     * @param trigger   Short human-readable description of the context
     *                  the user selected (e.g. "Work Stress", "Boredom").
     * @param timestamp Epoch milliseconds when the urge was detected.
     */
    data class UrgeDetected(
        val trigger: String,
        val timestamp: Long = System.currentTimeMillis()
    ) : AgentEvent()

    /**
     * Emitted by [CoachingAgent] when a new technique is being recommended.
     *
     * @param technique The coping technique the coaching agent has chosen.
     */
    data class CoachingUpdate(
        val technique: CoachingTechnique
    ) : AgentEvent()

    /**
     * Emitted by [ProgressAgent] whenever stats are recalculated.
     *
     * @param stats The latest habit statistics.
     */
    data class ProgressUpdate(
        val stats: ProgressStats
    ) : AgentEvent()

    /**
     * Emitted by [CoachingAgent] when the user completes a coaching session by
     * tapping "Done — I Resisted!". [RewardAgent] subscribes to this event to
     * select and display a reward message.
     *
     * @param totalSessions Running count of completed sessions (used to milestone rewards).
     * @param timestamp     Epoch milliseconds of completion.
     */
    data class SessionCompleted(
        val totalSessions: Int,
        val timestamp: Long = System.currentTimeMillis()
    ) : AgentEvent()
}
