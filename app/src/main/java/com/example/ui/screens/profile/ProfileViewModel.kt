package com.example.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.HabitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppThemeMode {
    SYSTEM, LIGHT, DARK
}

data class ProfileUiState(
    val userName: String = "Hamza",
    val userGoal: String = "Building consistent daily routines",
    val themeMode: AppThemeMode = AppThemeMode.LIGHT,
    val morningReminderEnabled: Boolean = true,
    val eveningReviewEnabled: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true,
    val weekStartsOnMonday: Boolean = true,
    val activeHabitsCount: Int = 0,
    val totalCheckInsCount: Int = 0
)

class ProfileViewModel(private val repository: HabitRepository) : ViewModel() {

    private val _themeMode = MutableStateFlow(AppThemeMode.LIGHT)
    val themeMode: StateFlow<AppThemeMode> = _themeMode

    private val _morningReminder = MutableStateFlow(true)
    private val _eveningReview = MutableStateFlow(true)
    private val _hapticFeedback = MutableStateFlow(true)
    private val _weekStartsOnMonday = MutableStateFlow(true)
    private val _userName = MutableStateFlow("Hamza")

    val uiState: StateFlow<ProfileUiState> = combine(
        repository.activeHabits,
        repository.allLogs,
        _themeMode,
        _morningReminder,
        _eveningReview
    ) { activeHabits, logs, theme, morningRem, eveningRev ->
        ProfileUiState(
            userName = _userName.value,
            userGoal = "Building consistent daily routines",
            themeMode = theme,
            morningReminderEnabled = morningRem,
            eveningReviewEnabled = eveningRev,
            hapticFeedbackEnabled = _hapticFeedback.value,
            weekStartsOnMonday = _weekStartsOnMonday.value,
            activeHabitsCount = activeHabits.size,
            totalCheckInsCount = logs.count { it.isCompleted }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProfileUiState()
    )

    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
    }

    fun toggleMorningReminder(enabled: Boolean) {
        _morningReminder.value = enabled
    }

    fun toggleEveningReview(enabled: Boolean) {
        _eveningReview.value = enabled
    }

    fun toggleHapticFeedback(enabled: Boolean) {
        _hapticFeedback.value = enabled
    }

    fun setWeekStartsOnMonday(startsMonday: Boolean) {
        _weekStartsOnMonday.value = startsMonday
    }

    fun updateUserName(name: String) {
        _userName.value = name
    }

    fun resetSampleData() {
        viewModelScope.launch {
            repository.resetToSampleData()
        }
    }
}
