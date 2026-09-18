package com.nailbitebreaker.agents

import com.nailbitebreaker.data.HabitRepository
import com.nailbitebreaker.data.UrgEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class ProgressAgentTest {

    private lateinit var repository: HabitRepository
    private lateinit var eventBus: MutableSharedFlow<AgentEvent>

    @Before
    fun setup() {
        repository = mock()
        eventBus = MutableSharedFlow(replay = 1)
    }

    private fun TestScope.createAgent() = ProgressAgent(
        agentScope = this,
        eventBus = eventBus,
        repository = repository,
        ioDispatcher = kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler),
        defaultDispatcher = kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler),
    )

    @Test
    fun `initial stats are empty`() = runTest {
        whenever(repository.getAllUrges()).thenReturn(emptyList())
        
        val agent = createAgent()
        agent.start()
        advanceUntilIdle()

        val stats = agent.stats.value
        assertEquals(0, stats.totalUrges)
        assertEquals(0, stats.currentStreak)
        
        agent.stop()
    }

    @Test
    fun `reacts to urge detected and updates stats`() = runTest {
        val now = System.currentTimeMillis()
        val event = UrgEvent(1, now, "Test", resolved = false)
        whenever(repository.getAllUrges()).thenReturn(listOf(event))
        
        val agent = createAgent()
        agent.start()
        
        eventBus.emit(AgentEvent.UrgeDetected("Test", now))
        advanceUntilIdle()

        val stats = agent.stats.value
        assertEquals(1, stats.totalUrges)
        assertEquals(1, stats.todayCount)
        
        agent.stop()
    }
}
