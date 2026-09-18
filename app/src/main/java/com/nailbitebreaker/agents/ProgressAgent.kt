package com.nailbitebreaker.agents

import com.nailbitebreaker.data.HabitRepository
import com.nailbitebreaker.data.UrgEvent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import java.util.Calendar

// ── Domain model ───────────────────────────────────────────────────────────────

/**
 * A snapshot of the user's habit-breaking progress.
 *
 * @param currentStreak Daily consecutive zero-urge days.
 * @param longestStreak All-time best streak.
 * @param todayCount    Number of urges recorded today.
 * @param totalUrges    Lifetime total of recorded urge events.
 * @param weeklyData    Urge count per day for the last 7 days.
 */
data class ProgressStats(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val todayCount: Int = 0,
    val totalUrges: Int = 0,
    val weeklyData: List<Int> = List(7) { 0 },
    val dailyStreaks: Map<Long, Int> = emptyMap() // Map of DayStartTimestamp -> Streak
)

// ── Progress Agent ─────────────────────────────────────────────────────────────

class ProgressAgent(
    private val agentScope: CoroutineScope,
    private val eventBus: SharedFlow<AgentEvent>,
    private val repository: HabitRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) : Agent {

    override val agentId: String = "progress-agent"

    private val _stats = MutableStateFlow(ProgressStats())
    val stats: StateFlow<ProgressStats> = _stats.asStateFlow()

    private var subscriptionJob: Job? = null

    override fun start() {
        agentScope.launch { refreshStats() }

        subscriptionJob = agentScope.launch {
            eventBus
                .filterIsInstance<AgentEvent.UrgeDetected>()
                .collect { event -> persistAndRefresh(event) }
        }
    }

    override fun stop() {
        subscriptionJob?.cancel()
        subscriptionJob = null
    }

    private suspend fun persistAndRefresh(event: AgentEvent.UrgeDetected) {
        withContext(ioDispatcher) {
            repository.insertUrge(
                UrgEvent(
                    timestamp = event.timestamp,
                    trigger = event.trigger,
                    resolved = false
                )
            )
        }
        refreshStats()
    }

    fun resetProgress() {
        agentScope.launch {
            withContext(ioDispatcher) {
                repository.clearAll()
            }
            refreshStats()
        }
    }

    private suspend fun refreshStats() = withContext(defaultDispatcher) {
        val allEvents = withContext(ioDispatcher) {
            repository.getAllUrges()
        }
        if (allEvents.isEmpty()) {
            _stats.value = ProgressStats()
            return@withContext
        }

        // Use a single calendar instance to avoid repeated allocations in loops
        val calendar = Calendar.getInstance()
        
        fun Long.toStartOfDay(): Long {
            calendar.timeInMillis = this
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            return calendar.timeInMillis
        }

        val startOfToday = System.currentTimeMillis().toStartOfDay()
        val dayMs = 86_400_000L

        // 1. Map all events to their start-of-day for O(1) lookup
        val urgeDays = HashSet<Long>(allEvents.size)
        allEvents.forEachIndexed { index, event ->
            urgeDays.add(event.timestamp.toStartOfDay())
            if (index % 100 == 0) yield()
        }

        val todayCount = allEvents.count { it.timestamp >= startOfToday }

        // 2. Compute Current Streak
        // We only count a streak if the user has logged something within the last 30 days
        // to avoid "zombie streaks" from abandoned app installs.
        var currentStreak = 0
        val lastUrgeTimestamp = allEvents.maxOf { it.timestamp }
        
        if (todayCount == 0 && (System.currentTimeMillis() - lastUrgeTimestamp) < (30 * dayMs)) {
            var cursor = startOfToday - dayMs
            val earliestTimestamp = allEvents.minOf { it.timestamp }
            
            while (cursor >= earliestTimestamp - dayMs) {
                if (urgeDays.contains(cursor)) break
                currentStreak++
                cursor -= dayMs
                if (currentStreak % 50 == 0) yield()
            }
        }

        // 3. Historical Data (Last 7 days)
        val weeklyData = (6 downTo 0).map { daysAgo ->
            val dayStart = startOfToday - daysAgo * dayMs
            val dayEnd = dayStart + dayMs
            allEvents.count { it.timestamp in dayStart until dayEnd }
        }

        // 4. Full History for Calendar & Longest Streak
        // Cap history to 365 days to prevent ANRs if the DB has very old/corrupt timestamps
        val dailyStreaks = mutableMapOf<Long, Int>()
        val oneYearAgo = startOfToday - (365 * dayMs)
        val earliestEvent = allEvents.minOf { it.timestamp }.toStartOfDay()
        val earliestToProcess = maxOf(earliestEvent, oneYearAgo)
        
        var dayCursor = earliestToProcess
        var runningStreak = 0
        var streakCount = 0
        
        while (dayCursor <= startOfToday) {
            if (!urgeDays.contains(dayCursor)) {
                runningStreak++
            } else {
                runningStreak = 0
            }
            dailyStreaks[dayCursor] = runningStreak
            dayCursor += dayMs
            
            streakCount++
            if (streakCount % 100 == 0) yield()
        }

        val longestStreak = dailyStreaks.values.maxOrNull() ?: 0

        _stats.value = ProgressStats(
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            todayCount = todayCount,
            totalUrges = allEvents.size,
            weeklyData = weeklyData,
            dailyStreaks = dailyStreaks
        )
    }
}
