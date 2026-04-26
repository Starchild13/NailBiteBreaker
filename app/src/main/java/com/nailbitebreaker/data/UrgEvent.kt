package com.nailbitebreaker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a single nail-biting urge event.
 *
 * Each row in the "urg_events" table corresponds to one time the user
 * tapped the urge button. The [ProgressAgent] reads these rows to compute
 * streaks and daily statistics.
 *
 * @param id        Auto-generated surrogate primary key.
 * @param timestamp Epoch milliseconds when the urge was detected.
 * @param trigger   Short context label selected by the user (e.g. "Work Stress").
 * @param resolved  True if the user successfully resisted the urge during the session.
 */
@Entity(tableName = "urg_events")
data class UrgEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val trigger: String,
    val resolved: Boolean
)
