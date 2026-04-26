package com.nailbitebreaker.data

/**
 * Single source of truth for all habit data in the application.
 *
 * Wraps [UrgEventDao] to decouple the agent layer from Room's API surface,
 * making it easy to swap in a different storage backend (or a fake for tests)
 * without touching any agent code.
 *
 * @param dao The generated Room DAO implementation.
 */
class HabitRepository(private val dao: UrgEventDao) {

    /**
     * Persists a new urge event to the local Room database.
     *
     * @param event The [UrgEvent] to store.
     */
    suspend fun insertUrge(event: UrgEvent) = dao.insert(event)

    /**
     * Returns every recorded urge event, ordered by timestamp descending.
     * [ProgressAgent] calls this after each new urge to recompute stats.
     */
    suspend fun getAllUrges(): List<UrgEvent> = dao.getAll()

    /**
     * Returns urge events within a specific time window.
     *
     * @param startMs Inclusive start in epoch milliseconds.
     * @param endMs   Exclusive end in epoch milliseconds.
     */
    suspend fun getUrgesBetween(startMs: Long, endMs: Long): List<UrgEvent> =
        dao.getBetween(startMs, endMs)

    /**
     * Erases all stored data — intended for a future "Reset Progress" user action.
     */
    suspend fun clearAll() = dao.deleteAll()
}
