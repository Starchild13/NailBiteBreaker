package com.nailbitebreaker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * Data Access Object for [UrgEvent] entities.
 *
 * All functions are suspend functions so Room runs them on a background thread
 * without blocking the main thread. Called exclusively from [HabitRepository].
 */
@Dao
interface UrgEventDao {

    /**
     * Inserts a new urge event into the database.
     * If an event with the same generated id somehow conflicts, it is replaced.
     *
     * @param event The [UrgEvent] to persist.
     * @return The row ID of the newly inserted item.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: UrgEvent): Long

    /**
     * Returns every recorded urge event, most recent first.
     * Used by [ProgressAgent] to compute stats.
     */
    @Query("SELECT * FROM urg_events ORDER BY timestamp DESC")
    suspend fun getAll(): List<UrgEvent>

    /**
     * Returns events whose timestamps fall within [startMs, endMs).
     * Used to bucket events per calendar day.
     *
     * @param startMs Inclusive lower bound (epoch milliseconds).
     * @param endMs   Exclusive upper bound (epoch milliseconds).
     */
    @Query("SELECT * FROM urg_events WHERE timestamp >= :startMs AND timestamp < :endMs")
    suspend fun getBetween(startMs: Long, endMs: Long): List<UrgEvent>

    /**
     * Deletes every row from the table.
     * Exposed so [HabitRepository.clearAll] can offer a "Reset Progress" feature.
     *
     * @return The number of rows deleted.
     */
    @Query("DELETE FROM urg_events")
    suspend fun deleteAll(): Int
}
