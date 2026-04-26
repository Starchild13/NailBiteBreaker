package com.nailbitebreaker

import android.app.Application
import com.nailbitebreaker.agents.AgentOrchestrator
import com.nailbitebreaker.data.HabitDatabase
import com.nailbitebreaker.data.HabitRepository

/**
 * Custom Application class that acts as the root dependency container.
 *
 * It wires up the database -> repository -> orchestrator chain using
 * lazy initialisation so objects are only created when first accessed.
 * The orchestrator is started in [onCreate] so all agents are live for
 * the entire app lifetime.
 */
class NailBiteBreakerApplication : Application() {

    /** Room database singleton — created lazily on first access. */
    val database: HabitDatabase by lazy {
        HabitDatabase.getInstance(this)
    }

    /** Repository wrapping the DAO. */
    val repository: HabitRepository by lazy {
        HabitRepository(database.urgEventDao())
    }

    /**
     * The central agent orchestrator that wires together all agents
     * via a shared coroutine event bus.
     */
    val orchestrator: AgentOrchestrator by lazy {
        AgentOrchestrator(repository)
    }

    override fun onCreate() {
        super.onCreate()
        // Start all agents when the application process is created.
        orchestrator.start()
    }

    override fun onTerminate() {
        super.onTerminate()
        // Gracefully stop all agents and cancel coroutine scopes.
        orchestrator.stop()
    }
}
