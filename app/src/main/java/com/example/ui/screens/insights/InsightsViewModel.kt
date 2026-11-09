package com.example.ui.screens.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.HabitEntity
import com.example.data.local.HabitLogEntity
import com.example.data.model.calculateCurrentStreak
import com.example.data.model.calculateLongestStreak
import com.example.data.model.isScheduledOn
import com.example.data.repository.HabitRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

data class DayCompletionStat(
    val date: LocalDate,
    val dayLabel: String,
    val completedCount: Int,
    val totalScheduled: Int,
    val completionPercentage: Float // 0.0 to 1.0
)

data class HabitPerformanceItem(
    val habit: HabitEntity,
    val currentStreak: Int,
    val longestStreak: Int,
    val completionRate30d: Int, // 0 - 100%
    val totalCompletions: Int
)

data class HeatmapDay(
    val date: LocalDate,
    val intensityLevel: Int // 0=none, 1=low, 2=medium, 3=high
)

data class InsightsUiState(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val overallCompletionRate: Int = 0,
    val totalCompletionsAllTime: Int = 0,
    val weeklyStats: List<DayCompletionStat> = emptyList(),
    val monthlyHeatmap: List<HeatmapDay> = emptyList(),
    val habitPerformances: List<HabitPerformanceItem> = emptyList(),
    val topPerformingHabitName: String? = null,
    val isLoading: Boolean = false
)

class InsightsViewModel(private val repository: HabitRepository) : ViewModel() {

    val uiState: StateFlow<InsightsUiState> = combine(
        repository.activeHabits,
        repository.allLogs
    ) { habits, logs ->
        val today = LocalDate.now()

        // 1. Weekly completion stats (past 7 days ending today)
        val weeklyStats = (6 downTo 0).map { offset ->
            val date = today.minusDays(offset.toLong())
            val scheduled = habits.count { it.isScheduledOn(date) }
            val completed = logs.count { it.epochDay == date.toEpochDay() && it.isCompleted }
            val pct = if (scheduled > 0) (completed.toFloat() / scheduled.toFloat()).coerceIn(0f, 1f) else 0f
            DayCompletionStat(
                date = date,
                dayLabel = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(1),
                completedCount = completed,
                totalScheduled = scheduled,
                completionPercentage = pct
            )
        }

        // 2. Monthly Heatmap (past 28 days = 4 full weeks)
        val monthlyHeatmap = (27 downTo 0).map { offset ->
            val date = today.minusDays(offset.toLong())
            val scheduled = habits.count { it.isScheduledOn(date) }
            val completed = logs.count { it.epochDay == date.toEpochDay() && it.isCompleted }
            val intensity = when {
                scheduled == 0 || completed == 0 -> 0
                completed >= scheduled -> 3
                completed >= scheduled / 2 -> 2
                else -> 1
            }
            HeatmapDay(date = date, intensityLevel = intensity)
        }

        // 3. Overall 30-day completion rate
        val thirtyDaysAgo = today.minusDays(29).toEpochDay()
        val scheduled30d = (0..29).sumOf { offset ->
            val d = today.minusDays(offset.toLong())
            habits.count { it.isScheduledOn(d) }
        }
        val completed30d = logs.count { it.epochDay in thirtyDaysAgo..today.toEpochDay() && it.isCompleted }
        val overallRate = if (scheduled30d > 0) ((completed30d.toFloat() / scheduled30d.toFloat()) * 100).toInt().coerceIn(0, 100) else 0

        // 4. Streaks
        val habitStreaks = habits.map { calculateCurrentStreak(it, logs, today) }
        val habitLongest = habits.map { calculateLongestStreak(it, logs) }
        val currentStreak = habitStreaks.maxOrNull() ?: 0
        val longestStreak = habitLongest.maxOrNull() ?: 0

        // 5. Individual Habit Performance
        val performances = habits.map { habit ->
            val hLogs = logs.filter { it.habitId == habit.id && it.isCompleted }
            val scheduledCount = (0..29).count { offset -> habit.isScheduledOn(today.minusDays(offset.toLong())) }
            val completedCount = hLogs.count { it.epochDay in thirtyDaysAgo..today.toEpochDay() }
            val rate = if (scheduledCount > 0) ((completedCount.toFloat() / scheduledCount.toFloat()) * 100).toInt().coerceIn(0, 100) else 0
            HabitPerformanceItem(
                habit = habit,
                currentStreak = calculateCurrentStreak(habit, logs, today),
                longestStreak = calculateLongestStreak(habit, logs),
                completionRate30d = rate,
                totalCompletions = hLogs.size
            )
        }.sortedByDescending { it.completionRate30d }

        val topHabit = performances.firstOrNull()?.habit?.name

        InsightsUiState(
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            overallCompletionRate = overallRate,
            totalCompletionsAllTime = logs.count { it.isCompleted },
            weeklyStats = weeklyStats,
            monthlyHeatmap = monthlyHeatmap,
            habitPerformances = performances,
            topPerformingHabitName = topHabit,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = InsightsUiState(isLoading = true)
    )
}
