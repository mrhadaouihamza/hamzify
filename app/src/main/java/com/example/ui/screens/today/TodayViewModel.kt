package com.example.ui.screens.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.HabitEntity
import com.example.data.local.HabitLogEntity
import com.example.data.model.calculateCurrentStreak
import com.example.data.model.isScheduledOn
import com.example.data.repository.HabitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class TodayHabitItem(
    val habit: HabitEntity,
    val isCompleted: Boolean,
    val isSkipped: Boolean,
    val isOverdue: Boolean,
    val streak: Int
)

data class TodayUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val uncompletedHabits: List<TodayHabitItem> = emptyList(),
    val completedHabits: List<TodayHabitItem> = emptyList(),
    val totalCount: Int = 0,
    val completedCount: Int = 0,
    val highestActiveStreak: Int = 0,
    val completionMap: Map<Long, Boolean> = emptyMap(),
    val allLogs: List<HabitLogEntity> = emptyList(),
    val isLoading: Boolean = false
) {
    val habits: List<TodayHabitItem>
        get() = uncompletedHabits + completedHabits
}

class TodayViewModel(private val repository: HabitRepository) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate

    val uiState: StateFlow<TodayUiState> = combine(
        _selectedDate,
        repository.activeHabits,
        repository.allLogs
    ) { date, activeHabits, allLogs ->
        val today = LocalDate.now()
        // Filter habits that are scheduled on the selected date
        val scheduledHabits = activeHabits.filter { it.isScheduledOn(date) }

        val logsOnDate = allLogs.filter { it.epochDay == date.toEpochDay() }
        val completedIdsOnDate = logsOnDate.filter { it.isCompleted }.map { it.habitId }.toSet()
        val skippedIdsOnDate = logsOnDate.filter { it.isSkipped }.map { it.habitId }.toSet()

        val items = scheduledHabits.map { habit ->
            val isCompleted = completedIdsOnDate.contains(habit.id)
            val isSkipped = skippedIdsOnDate.contains(habit.id)
            val streak = calculateCurrentStreak(habit, allLogs, date)

            // Overdue Recognition:
            // - If viewing a past date: any habit not completed or skipped is automatically Overdue.
            // - If viewing today: if a past scheduled day was missed without completion or skip, it's recognized as Overdue until completed.
            val isOverdue = when {
                isCompleted || isSkipped -> false
                date.isBefore(today) -> true
                date.isEqual(today) -> {
                    var check = today.minusDays(1)
                    var missedPast = false
                    while (check.toEpochDay() >= habit.startDateEpochDay && check.isAfter(today.minusDays(7))) {
                        if (habit.isScheduledOn(check)) {
                            val log = allLogs.find { it.habitId == habit.id && it.epochDay == check.toEpochDay() }
                            if (log == null || (!log.isCompleted && !log.isSkipped)) {
                                missedPast = true
                            }
                            break
                        }
                        check = check.minusDays(1)
                    }
                    missedPast
                }
                else -> false
            }

            TodayHabitItem(
                habit = habit,
                isCompleted = isCompleted,
                isSkipped = isSkipped,
                isOverdue = isOverdue,
                streak = streak
            )
        }

        // Split into Top Section (Active / Uncompleted) and Bottom Section (Completed)
        val uncompletedHabits = items.filter { !it.isCompleted }
        val completedHabits = items.filter { it.isCompleted }

        // Completion map for nearby days for date strip dot indicator
        val completionMap = allLogs
            .filter { it.isCompleted }
            .groupBy { it.epochDay }
            .mapValues { true }

        val completedCount = completedHabits.size
        val highestActiveStreak = items.maxOfOrNull { it.streak } ?: 0

        TodayUiState(
            selectedDate = date,
            uncompletedHabits = uncompletedHabits,
            completedHabits = completedHabits,
            totalCount = items.size,
            completedCount = completedCount,
            highestActiveStreak = highestActiveStreak,
            completionMap = completionMap,
            allLogs = allLogs,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TodayUiState(isLoading = true)
    )

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun toggleHabit(habitId: Long) {
        viewModelScope.launch {
            repository.toggleHabitCompletion(habitId, _selectedDate.value.toEpochDay())
        }
    }

    fun markDone(habitId: Long) {
        viewModelScope.launch {
            repository.markHabitCompletion(habitId, _selectedDate.value.toEpochDay(), true)
        }
    }

    fun markSkip(habitId: Long) {
        viewModelScope.launch {
            repository.markHabitSkipped(habitId, _selectedDate.value.toEpochDay())
        }
    }

    fun createHabit(habit: HabitEntity) {
        viewModelScope.launch {
            repository.insertHabit(habit)
        }
    }

    fun updateHabit(habit: HabitEntity) {
        viewModelScope.launch {
            repository.updateHabit(habit)
        }
    }

    fun deleteHabit(habitId: Long) {
        viewModelScope.launch {
            repository.deleteHabit(habitId)
        }
    }
}
