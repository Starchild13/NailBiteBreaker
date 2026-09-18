package com.nailbitebreaker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.nailbitebreaker.NailBiteBreakerApplication
import com.nailbitebreaker.agents.AgentOrchestrator
import com.nailbitebreaker.agents.ProgressStats
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel for [ProgressScreen].
 *
 * Provides [ProgressStats] from [ProgressAgent] so the progress dashboard
 * can display streak counts, the weekly bar chart, and the urge history —
 * without any direct coupling to Room or the agent implementation.
 */
class ProgressViewModel(application: Application) : AndroidViewModel(application) {

    private val orchestrator: AgentOrchestrator =
        (application as NailBiteBreakerApplication).orchestrator

    /**
     * Live habit statistics from [ProgressAgent].
     * Updated automatically after every urge event is persisted to Room.
     */
    val stats: StateFlow<ProgressStats> = orchestrator.progressAgent.stats

    /**
     * Resets all progress data by clearing the database and refreshing stats to zero.
     */
    fun resetProgress() {
        orchestrator.progressAgent.resetProgress()
    }
}
