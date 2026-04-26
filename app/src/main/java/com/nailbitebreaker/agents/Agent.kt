package com.nailbitebreaker.agents

/**
 * Base contract that every agent in the multi-agent system must fulfil.
 *
 * Each agent encapsulates a single, well-defined responsibility (detection,
 * coaching, or progress tracking). Agents communicate exclusively through
 * the [AgentOrchestrator]'s shared event bus rather than calling each
 * other directly, keeping coupling to a minimum.
 */
interface Agent {

    /**
     * A unique, human-readable identifier for this agent.
     * Used for logging and debugging purposes.
     */
    val agentId: String

    /**
     * Called once by [AgentOrchestrator.start] when the app process launches.
     * Implementations should launch their coroutine subscriptions here.
     */
    fun start()

    /**
     * Called once by [AgentOrchestrator.stop] when the app is terminating.
     * Implementations should cancel any running coroutine jobs here.
     */
    fun stop()
}
