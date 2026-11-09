package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val category: String = "Health & Wellness",
    val iconName: String = "water_drop",
    val colorHex: String = "#6C63FF",
    val frequencyType: String = "DAILY", // DAILY, WEEKDAYS, WEEKENDS, CUSTOM
    val targetDaysOfWeek: String = "1,2,3,4,5,6,7", // 1=Monday .. 7=Sunday
    val targetCountPerDay: Int = 1,
    val unit: String = "times",
    val timeOfDay: String = "ANYTIME", // MORNING, AFTERNOON, EVENING, ANYTIME
    val reminderTime: String? = null,
    val startDateEpochDay: Long,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
