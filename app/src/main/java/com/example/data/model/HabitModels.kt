package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.data.local.HabitEntity
import com.example.data.local.HabitLogEntity
import java.time.LocalDate

data class HabitItem(
    val habit: HabitEntity,
    val isCompletedToday: Boolean,
    val currentStreak: Int,
    val isScheduledForSelectedDate: Boolean
)

data class HabitCategoryItem(
    val name: String,
    val iconName: String,
    val defaultColor: String
)

val PredefinedCategories = listOf(
    HabitCategoryItem("Health & Wellness", "water_drop", "#00B4D8"),
    HabitCategoryItem("Mind & Focus", "self_improvement", "#6C63FF"),
    HabitCategoryItem("Fitness & Body", "fitness_center", "#FF5D8F"),
    HabitCategoryItem("Productivity", "bolt", "#FF9F1C"),
    HabitCategoryItem("Personal Growth", "menu_book", "#2EC4B6"),
    HabitCategoryItem("Lifestyle & Sleep", "bedtime", "#9D4EDD")
)

val PredefinedIcons = listOf(
    "water_drop" to "Hydration",
    "self_improvement" to "Meditation",
    "menu_book" to "Reading",
    "fitness_center" to "Workout",
    "directions_run" to "Running",
    "bedtime" to "Sleep",
    "bolt" to "Energy",
    "spa" to "Relaxation",
    "psychology" to "Brain / Mind",
    "code" to "Coding",
    "brush" to "Creativity",
    "local_cafe" to "Coffee / Routine",
    "music_note" to "Music / Audio",
    "star" to "General / Goal"
)

fun getIconVector(iconName: String): ImageVector {
    return when (iconName) {
        "water_drop" -> Icons.Rounded.WaterDrop
        "self_improvement" -> Icons.Rounded.SelfImprovement
        "menu_book" -> Icons.Rounded.MenuBook
        "fitness_center" -> Icons.Rounded.FitnessCenter
        "directions_run" -> Icons.Rounded.DirectionsRun
        "bedtime" -> Icons.Rounded.Bedtime
        "bolt" -> Icons.Rounded.Bolt
        "spa" -> Icons.Rounded.Spa
        "psychology" -> Icons.Rounded.Psychology
        "code" -> Icons.Rounded.Code
        "brush" -> Icons.Rounded.Brush
        "local_cafe" -> Icons.Rounded.LocalCafe
        "music_note" -> Icons.Rounded.MusicNote
        else -> Icons.Rounded.Star
    }
}

/**
 * Checks if a habit is scheduled on a given local date.
 */
fun HabitEntity.isScheduledOn(date: LocalDate): Boolean {
    if (date.toEpochDay() < startDateEpochDay) return false
    val dayOfWeek = date.dayOfWeek.value // 1 (Mon) - 7 (Sun)
    return when (frequencyType) {
        "DAILY" -> true
        "WEEKDAYS" -> dayOfWeek in 1..5
        "WEEKENDS" -> dayOfWeek in 6..7
        "CUSTOM" -> {
            val days = targetDaysOfWeek.split(",").mapNotNull { it.trim().toIntOrNull() }
            dayOfWeek in days
        }
        else -> true
    }
}

/**
 * Calculates current consecutive streak for a habit ending at or before today.
 * Skipped habits are protected in system logic so they DO NOT break the streak.
 */
fun calculateCurrentStreak(
    habit: HabitEntity,
    logs: List<HabitLogEntity>,
    today: LocalDate = LocalDate.now()
): Int {
    val completedEpochDays = logs.filter { it.habitId == habit.id && it.isCompleted }
        .map { it.epochDay }
        .toSet()
    val skippedEpochDays = logs.filter { it.habitId == habit.id && it.isSkipped }
        .map { it.epochDay }
        .toSet()

    var streak = 0
    var checkDate = today

    // Check today:
    val todayCompleted = completedEpochDays.contains(checkDate.toEpochDay())
    if (todayCompleted) {
        streak++
        checkDate = checkDate.minusDays(1)
    } else {
        // Today is skipped or not yet done; start checking backwards from yesterday
        checkDate = checkDate.minusDays(1)
    }

    while (true) {
        if (checkDate.toEpochDay() < habit.startDateEpochDay) break
        if (habit.isScheduledOn(checkDate)) {
            val dateEpoch = checkDate.toEpochDay()
            if (completedEpochDays.contains(dateEpoch)) {
                streak++
                checkDate = checkDate.minusDays(1)
            } else if (skippedEpochDays.contains(dateEpoch)) {
                // Skipped day: PROTECTED! Bridges to previous days without breaking streak
                checkDate = checkDate.minusDays(1)
            } else {
                // Missed day: streak broken
                break
            }
        } else {
            // Unscheduled day doesn't break the streak
            checkDate = checkDate.minusDays(1)
        }
    }

    return streak
}

/**
 * Calculates the longest streak achieved for a habit.
 * Skipped days preserve the streak continuity.
 */
fun calculateLongestStreak(
    habit: HabitEntity,
    logs: List<HabitLogEntity>
): Int {
    val completedEpochDays = logs.filter { it.habitId == habit.id && it.isCompleted }
        .map { it.epochDay }
        .toSet()
    val skippedEpochDays = logs.filter { it.habitId == habit.id && it.isSkipped }
        .map { it.epochDay }
        .toSet()

    if (completedEpochDays.isEmpty()) return 0

    val minEpochDay = habit.startDateEpochDay.coerceAtMost(completedEpochDays.minOrNull() ?: 0L)
    val maxEpochDay = LocalDate.now().toEpochDay()

    var longest = 0
    var current = 0

    var currentDay = LocalDate.ofEpochDay(minEpochDay)
    val endDay = LocalDate.ofEpochDay(maxEpochDay)

    while (!currentDay.isAfter(endDay)) {
        if (habit.isScheduledOn(currentDay)) {
            val dateEpoch = currentDay.toEpochDay()
            if (completedEpochDays.contains(dateEpoch)) {
                current++
                if (current > longest) longest = current
            } else if (skippedEpochDays.contains(dateEpoch)) {
                // Skipped day preserves current streak
            } else {
                current = 0
            }
        }
        currentDay = currentDay.plusDays(1)
    }

    return longest
}
