package com.nailbitebreaker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.nailbitebreaker.NailBiteBreakerApplication
import com.nailbitebreaker.agents.AgentOrchestrator
import com.nailbitebreaker.agents.CoachingState
import com.nailbitebreaker.agents.RewardState
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel for [CoachScreen].
 *
 * Exposes [CoachingAgent.state] and [RewardAgent.state] as [StateFlow]s so
 * [CoachScreen] can reactively render techniques and rewards.
 */
class CoachViewModel(application: Application) : AndroidViewModel(application) {

    private val orchestrator: AgentOrchestrator =
        (application as NailBiteBreakerApplication).orchestrator

    /**
     * Live coaching state from [CoachingAgent].
     */
    val coachingState: StateFlow<CoachingState> = orchestrator.coachingAgent.state

    /**
     * Live reward state from [RewardAgent].
     */
    val rewardState: StateFlow<RewardState> = orchestrator.rewardAgent.state

    /**
     * Asks [CoachingAgent] to advance to the next technique in rotation.
     */
    fun nextTechnique() = orchestrator.coachingAgent.nextTechnique()

    /**
     * Marks the current session as done and triggers the reward event.
     */
    fun dismissSession() = orchestrator.coachingAgent.dismissSession()

    /**
     * Hides the reward overlay.
     */
    fun dismissReward() = orchestrator.rewardAgent.dismissReward()
}
