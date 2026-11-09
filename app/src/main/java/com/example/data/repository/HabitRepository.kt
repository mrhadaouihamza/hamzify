package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.HabitDao
import com.example.data.local.HabitEntity
import com.example.data.local.HabitLogEntity
import kotlinx.coroutines.flow.Flow

class HabitRepository(private val habitDao: HabitDao) {

    val allHabits: Flow<List<HabitEntity>> = habitDao.getAllHabits()
    val activeHabits: Flow<List<HabitEntity>> = habitDao.getActiveHabits()
    val archivedHabits: Flow<List<HabitEntity>> = habitDao.getArchivedHabits()
    val allLogs: Flow<List<HabitLogEntity>> = habitDao.getAllLogs()

    fun getHabitById(id: Long): Flow<HabitEntity?> = habitDao.getHabitById(id)

    fun getLogsForDate(epochDay: Long): Flow<List<HabitLogEntity>> =
        habitDao.getLogsForDate(epochDay)

    fun getLogsForDateRange(startEpochDay: Long, endEpochDay: Long): Flow<List<HabitLogEntity>> =
        habitDao.getLogsForDateRange(startEpochDay, endEpochDay)

    fun getLogsForHabit(habitId: Long): Flow<List<HabitLogEntity>> =
        habitDao.getLogsForHabit(habitId)

    suspend fun insertHabit(habit: HabitEntity): Long = habitDao.insertHabit(habit)

    suspend fun updateHabit(habit: HabitEntity) = habitDao.updateHabit(habit)

    suspend fun deleteHabit(habitId: Long) {
        habitDao.deleteLogsForHabit(habitId)
        habitDao.deleteHabitById(habitId)
    }

    suspend fun setArchived(habitId: Long, isArchived: Boolean) {
        habitDao.setHabitArchived(habitId, isArchived)
    }

    suspend fun toggleHabitCompletion(habitId: Long, epochDay: Long): Boolean {
        val existing = habitDao.getLog(habitId, epochDay)
        return if (existing != null && existing.isCompleted) {
            habitDao.deleteLog(habitId, epochDay)
            false
        } else {
            habitDao.insertLog(
                HabitLogEntity(
                    id = existing?.id ?: 0,
                    habitId = habitId,
                    epochDay = epochDay,
                    completedCount = 1,
                    isCompleted = true,
                    isSkipped = false,
                    completedAt = System.currentTimeMillis()
                )
            )
            true
        }
    }

    suspend fun markHabitCompletion(habitId: Long, epochDay: Long, isCompleted: Boolean) {
        val existing = habitDao.getLog(habitId, epochDay)
        if (isCompleted) {
            habitDao.insertLog(
                HabitLogEntity(
                    id = existing?.id ?: 0,
                    habitId = habitId,
                    epochDay = epochDay,
                    completedCount = 1,
                    isCompleted = true,
                    isSkipped = false,
                    completedAt = System.currentTimeMillis()
                )
            )
        } else {
            habitDao.deleteLog(habitId, epochDay)
        }
    }

    suspend fun markHabitSkipped(habitId: Long, epochDay: Long) {
        val existing = habitDao.getLog(habitId, epochDay)
        if (existing != null && existing.isSkipped) {
            // Unskip
            habitDao.deleteLog(habitId, epochDay)
        } else {
            habitDao.insertLog(
                HabitLogEntity(
                    id = existing?.id ?: 0,
                    habitId = habitId,
                    epochDay = epochDay,
                    completedCount = 0,
                    isCompleted = false,
                    isSkipped = true,
                    completedAt = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun resetToSampleData() {
        habitDao.clearLogs()
        habitDao.clearHabits()
        AppDatabase.seedInitialData(habitDao)
    }
}
