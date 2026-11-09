package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Query("SELECT * FROM habits ORDER BY id ASC")
    fun getAllHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE isArchived = 0 ORDER BY id ASC")
    fun getActiveHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE isArchived = 1 ORDER BY id ASC")
    fun getArchivedHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id = :id LIMIT 1")
    fun getHabitById(id: Long): Flow<HabitEntity?>

    @Query("SELECT * FROM habits WHERE id = :id LIMIT 1")
    suspend fun getHabitByIdSync(id: Long): HabitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabits(habits: List<HabitEntity>): List<Long>

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Delete
    suspend fun deleteHabit(habit: HabitEntity)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteHabitById(id: Long)

    @Query("UPDATE habits SET isArchived = :isArchived WHERE id = :id")
    suspend fun setHabitArchived(id: Long, isArchived: Boolean)

    @Query("SELECT * FROM habit_logs")
    fun getAllLogs(): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE epochDay = :epochDay")
    fun getLogsForDate(epochDay: Long): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE epochDay BETWEEN :startEpochDay AND :endEpochDay")
    fun getLogsForDateRange(startEpochDay: Long, endEpochDay: Long): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId ORDER BY epochDay DESC")
    fun getLogsForHabit(habitId: Long): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND epochDay = :epochDay LIMIT 1")
    suspend fun getLog(habitId: Long, epochDay: Long): HabitLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: HabitLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<HabitLogEntity>)

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId AND epochDay = :epochDay")
    suspend fun deleteLog(habitId: Long, epochDay: Long)

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId")
    suspend fun deleteLogsForHabit(habitId: Long)

    @Query("DELETE FROM habits")
    suspend fun clearHabits()

    @Query("DELETE FROM habit_logs")
    suspend fun clearLogs()
}
