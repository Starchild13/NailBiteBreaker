package com.nailbitebreaker.agents

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CoachingAgentTest {

    private lateinit var eventBus: MutableSharedFlow<AgentEvent>
    private val emittedEvents = mutableListOf<AgentEvent>()

    @Before
    fun setup() {
        eventBus = MutableSharedFlow(replay = 1)
        emittedEvents.clear()
    }

    private fun TestScope.createAgent() = CoachingAgent(
        agentScope = this,
        eventBus = eventBus,
        emit = { emittedEvents.add(it) },
    )

    @Test
    fun `initial state is inactive`() = runTest {
        val agent = createAgent()
        val state = agent.state.value
        assertFalse(state.isActive)
        assertEquals(0, state.sessionCount)
        assertEquals(null, state.technique)
    }

    @Test
    fun `reacts to urge detected event`() = runTest {
        val agent = createAgent()
        agent.start()
        
        eventBus.emit(AgentEvent.UrgeDetected(trigger = "Test Trigger"))
        advanceUntilIdle()

        val state = agent.state.value
        assertTrue("State should be active after urge detected", state.isActive)
        assertEquals(1, state.sessionCount)
        assertNotNull(state.technique)
        
        agent.stop()
    }

    @Test
    fun `cycling through techniques updates state`() = runTest {
        val agent = createAgent()
        agent.start()
        
        eventBus.emit(AgentEvent.UrgeDetected(trigger = "Test"))
        advanceUntilIdle()
        
        val firstTechnique = agent.state.value.technique
        
        agent.nextTechnique()
        val secondTechnique = agent.state.value.technique
        
        assertTrue(firstTechnique != secondTechnique)
        
        agent.stop()
    }

    @Test
    fun `dismissing session emits completion event`() = runTest {
        val agent = createAgent()
        agent.start()
        
        eventBus.emit(AgentEvent.UrgeDetected(trigger = "Test"))
        advanceUntilIdle()
        
        agent.dismissSession()
        
        assertFalse(agent.state.value.isActive)
        assertEquals(1, emittedEvents.size)
        assertTrue(emittedEvents[0] is AgentEvent.SessionCompleted)
        assertEquals(1, (emittedEvents[0] as AgentEvent.SessionCompleted).totalSessions)
        
        agent.stop()
    }
}
