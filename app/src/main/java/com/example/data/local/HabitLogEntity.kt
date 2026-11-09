package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "habit_logs",
    indices = [
        Index(value = ["habitId", "epochDay"], unique = true),
        Index(value = ["epochDay"])
    ]
)
data class HabitLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: Long,
    val epochDay: Long,
    val completedCount: Int = 1,
    val isCompleted: Boolean = true,
    val isSkipped: Boolean = false,
    val completedAt: Long = System.currentTimeMillis()
)
