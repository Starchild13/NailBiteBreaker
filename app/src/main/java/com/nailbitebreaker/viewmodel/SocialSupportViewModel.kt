package com.nailbitebreaker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.nailbitebreaker.NailBiteBreakerApplication
import com.nailbitebreaker.agents.SocialSupportState
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel for [SocialChatScreen].
 */
class SocialSupportViewModel(application: Application) : AndroidViewModel(application) {

    private val orchestrator = (application as NailBiteBreakerApplication).orchestrator
    private val agent = orchestrator.socialSupportAgent

    /**
     * Live chat state from [SocialSupportAgent].
     */
    val state: StateFlow<SocialSupportState> = agent.state

    /**
     * Sends a message to the agent.
     */
    fun sendMessage(text: String) {
        agent.sendMessage(text)
    }
}
