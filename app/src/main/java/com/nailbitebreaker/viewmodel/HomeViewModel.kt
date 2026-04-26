package com.nailbitebreaker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.nailbitebreaker.NailBiteBreakerApplication
import com.nailbitebreaker.agents.AgentOrchestrator
import com.nailbitebreaker.agents.ProgressStats
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel for [HomeScreen].
 *
 * Acts as the bridge between the UI and two agents:
 *   - [DetectionAgent]: forwards urge button taps as [AgentEvent.UrgeDetected].
 *   - [ProgressAgent]: exposes [ProgressStats] so the streak badge stays live.
 *
 * Using [AndroidViewModel] grants access to the [Application] instance, from which
 * we retrieve the application-scoped [AgentOrchestrator] — no Hilt required.
 */
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val orchestrator: AgentOrchestrator =
        (application as NailBiteBreakerApplication).orchestrator

    /**
     * Live progress stats from [ProgressAgent].
     * Collected by [HomeScreen] to keep the streak badge and today's count current.
     */
    val stats: StateFlow<ProgressStats> = orchestrator.progressAgent.stats

    /**
     * Reports an urge event to [DetectionAgent], which publishes it onto the
     * inter-agent event bus so [CoachingAgent] and [ProgressAgent] can react.
     *
     * @param trigger Short context label (e.g. "Work Stress", "Boredom").
     */
    fun reportUrge(trigger: String) {
        orchestrator.detectionAgent.reportUrge(trigger)
    }
}
