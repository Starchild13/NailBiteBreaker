package com.nailbitebreaker.agents

import com.nailbitebreaker.data.HabitRepository
import com.nailbitebreaker.data.UrgEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone

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
    private val repository: HabitRepository
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
        repository.insertUrge(
            UrgEvent(
                timestamp = event.timestamp,
                trigger = event.trigger,
                resolved = false
            )
        )
        refreshStats()
    }

    private suspend fun refreshStats() {
        val allEvents = repository.getAllUrges()
        val now = System.currentTimeMillis()
        
        // Use local timezone for "start of today" to match user's perspective
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfToday = calendar.timeInMillis
        val dayMs = 86_400_000L

        val todayCount = allEvents.count { it.timestamp >= startOfToday }

        // Recalibrate streak: Reset to 0 if an urge happened today.
        // The "currentStreak" now reflects "How many days have I gone WITHOUT biting?"
        // If they bit today, streak is 0.
        val currentStreak = if (todayCount > 0) 0 else computeStreakBackFrom(allEvents, startOfToday, dayMs)

        // Historical Data for the last 7 days
        val weeklyData = (6 downTo 0).map { daysAgo ->
            val dayStart = startOfToday - daysAgo * dayMs
            val dayEnd = dayStart + dayMs
            allEvents.count { it.timestamp in dayStart until dayEnd }
        }

        // Full History for Calendar (Map of day -> streak on that day)
        val dailyStreaks = computeDailyStreakHistory(allEvents, startOfToday, dayMs)
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

    private fun computeStreakBackFrom(events: List<UrgEvent>, startOfToday: Long, dayMs: Long): Int {
        var streak = 0
        var dayCursor = startOfToday - dayMs
        while (dayCursor >= 0) {
            val dayEnd = dayCursor + dayMs
            val hasUrge = events.any { it.timestamp in dayCursor until dayEnd }
            if (hasUrge) break
            streak++
            dayCursor -= dayMs
        }
        return streak
    }

    private fun computeDailyStreakHistory(events: List<UrgEvent>, startOfToday: Long, dayMs: Long): Map<Long, Int> {
        if (events.isEmpty()) return emptyMap()
        
        val history = mutableMapOf<Long, Int>()
        val earliest = events.minOf { it.timestamp }
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = earliest
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        var dayCursor = calendar.timeInMillis
        var runningStreak = 0
        
        while (dayCursor <= startOfToday) {
            val dayEnd = dayCursor + dayMs
            val hasUrge = events.any { it.timestamp in dayCursor until dayEnd }
            
            if (!hasUrge) {
                runningStreak++
            } else {
                runningStreak = 0
            }
            
            history[dayCursor] = runningStreak
            dayCursor += dayMs
        }
        
        return history
    }
}
