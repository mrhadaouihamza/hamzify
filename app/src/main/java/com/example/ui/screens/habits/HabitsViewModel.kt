package com.example.ui.screens.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.HabitEntity
import com.example.data.local.HabitLogEntity
import com.example.data.model.calculateCurrentStreak
import com.example.data.model.calculateLongestStreak
import com.example.data.repository.HabitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HabitDetailItem(
    val habit: HabitEntity,
    val currentStreak: Int,
    val longestStreak: Int,
    val totalCompletions: Int
)

data class HabitsUiState(
    val habits: List<HabitDetailItem> = emptyList(),
    val searchQuery: String = "",
    val showArchived: Boolean = false,
    val allLogs: List<HabitLogEntity> = emptyList(),
    val isLoading: Boolean = false
)

class HabitsViewModel(private val repository: HabitRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _showArchived = MutableStateFlow(false)

    val uiState: StateFlow<HabitsUiState> = combine(
        repository.allHabits,
        repository.allLogs,
        _searchQuery,
        _showArchived
    ) { habits, logs, query, showArchived ->
        val filtered = habits.filter { habit ->
            // Active vs Archived filter
            (habit.isArchived == showArchived) &&
            // Search query filter
            (query.isBlank() || habit.name.contains(query, ignoreCase = true) || habit.description.contains(query, ignoreCase = true))
        }

        val items = filtered.map { habit ->
            val habitLogs = logs.filter { it.habitId == habit.id && it.isCompleted }
            HabitDetailItem(
                habit = habit,
                currentStreak = calculateCurrentStreak(habit, logs),
                longestStreak = calculateLongestStreak(habit, logs),
                totalCompletions = habitLogs.size
            )
        }

        HabitsUiState(
            habits = items,
            searchQuery = query,
            showArchived = showArchived,
            allLogs = logs,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HabitsUiState(isLoading = true)
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onToggleShowArchived(showArchived: Boolean) {
        _showArchived.value = showArchived
    }

    fun insertHabit(habit: HabitEntity) {
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

    fun setArchived(habitId: Long, isArchived: Boolean) {
        viewModelScope.launch {
            repository.setArchived(habitId, isArchived)
        }
    }

    fun toggleHabit(habitId: Long) {
        viewModelScope.launch {
            repository.toggleHabitCompletion(habitId, java.time.LocalDate.now().toEpochDay())
        }
    }
}
