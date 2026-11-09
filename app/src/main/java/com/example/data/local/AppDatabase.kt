package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

@Database(
    entities = [HabitEntity::class, HabitLogEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun habitDao(): HabitDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "habit_flow_database.db"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        seedInitialData(database.habitDao())
                    }
                }
            }
        }

        suspend fun seedInitialData(dao: HabitDao) {
            val today = LocalDate.now().toEpochDay()
            val habits = listOf(
                HabitEntity(
                    name = "Morning Meditation",
                    description = "10 minutes of mindfulness breathing",
                    category = "Mind & Focus",
                    iconName = "self_improvement",
                    colorHex = "#6C63FF",
                    frequencyType = "DAILY",
                    targetDaysOfWeek = "1,2,3,4,5,6,7",
                    targetCountPerDay = 1,
                    unit = "mins",
                    timeOfDay = "MORNING",
                    reminderTime = "07:30",
                    startDateEpochDay = today - 30,
                    isArchived = false
                ),
                HabitEntity(
                    name = "Hydrate 2L Water",
                    description = "Stay refreshed throughout the day",
                    category = "Health & Wellness",
                    iconName = "water_drop",
                    colorHex = "#00B4D8",
                    frequencyType = "DAILY",
                    targetDaysOfWeek = "1,2,3,4,5,6,7",
                    targetCountPerDay = 1,
                    unit = "liters",
                    timeOfDay = "ANYTIME",
                    reminderTime = "09:00",
                    startDateEpochDay = today - 25,
                    isArchived = false
                ),
                HabitEntity(
                    name = "30-min Reading",
                    description = "Read non-fiction or philosophy books",
                    category = "Personal Growth",
                    iconName = "menu_book",
                    colorHex = "#2EC4B6",
                    frequencyType = "DAILY",
                    targetDaysOfWeek = "1,2,3,4,5,6,7",
                    targetCountPerDay = 1,
                    unit = "mins",
                    timeOfDay = "EVENING",
                    reminderTime = "21:00",
                    startDateEpochDay = today - 20,
                    isArchived = false
                ),
                HabitEntity(
                    name = "Strength & Mobility",
                    description = "Bodyweight exercises and stretching",
                    category = "Fitness & Body",
                    iconName = "fitness_center",
                    colorHex = "#FF5D8F",
                    frequencyType = "CUSTOM",
                    targetDaysOfWeek = "1,2,3,4,5",
                    targetCountPerDay = 1,
                    unit = "workout",
                    timeOfDay = "MORNING",
                    reminderTime = "08:15",
                    startDateEpochDay = today - 20,
                    isArchived = false
                ),
                HabitEntity(
                    name = "Digital Wind-down",
                    description = "No blue light 45 min before sleep",
                    category = "Lifestyle & Sleep",
                    iconName = "bedtime",
                    colorHex = "#9D4EDD",
                    frequencyType = "DAILY",
                    targetDaysOfWeek = "1,2,3,4,5,6,7",
                    targetCountPerDay = 1,
                    unit = "routine",
                    timeOfDay = "EVENING",
                    reminderTime = "22:30",
                    startDateEpochDay = today - 15,
                    isArchived = false
                )
            )

            val insertedIds = dao.insertHabits(habits)

            // Seed logs for the past 14 days up to today
            val logs = mutableListOf<HabitLogEntity>()
            for (offset in 14 downTo 1) {
                val day = today - offset
                val dayOfWeek = LocalDate.ofEpochDay(day).dayOfWeek.value // 1=Mon..7=Sun

                // Morning Meditation: high consistency
                if (offset != 3) {
                    logs.add(
                        HabitLogEntity(
                            habitId = insertedIds.getOrElse(0) { 1L },
                            epochDay = day,
                            isCompleted = true
                        )
                    )
                }

                // Hydrate: high consistency
                if (offset != 5 && offset != 9) {
                    logs.add(
                        HabitLogEntity(
                            habitId = insertedIds.getOrElse(1) { 2L },
                            epochDay = day,
                            isCompleted = true
                        )
                    )
                }

                // Reading: almost daily
                if (offset != 2 && offset != 8) {
                    logs.add(
                        HabitLogEntity(
                            habitId = insertedIds.getOrElse(2) { 3L },
                            epochDay = day,
                            isCompleted = true
                        )
                    )
                }

                // Strength & Mobility: weekdays
                if (dayOfWeek in 1..5 && offset != 4) {
                    logs.add(
                        HabitLogEntity(
                            habitId = insertedIds.getOrElse(3) { 4L },
                            epochDay = day,
                            isCompleted = true
                        )
                    )
                }

                // Digital Wind-down
                if (offset != 1 && offset != 6) {
                    logs.add(
                        HabitLogEntity(
                            habitId = insertedIds.getOrElse(4) { 5L },
                            epochDay = day,
                            isCompleted = true
                        )
                    )
                }
            }

            // For today: mark 2 habits as completed initially so user sees partial progress immediately!
            if (insertedIds.isNotEmpty()) {
                logs.add(
                    HabitLogEntity(
                        habitId = insertedIds[0],
                        epochDay = today,
                        isCompleted = true
                    )
                )
            }
            if (insertedIds.size > 1) {
                logs.add(
                    HabitLogEntity(
                        habitId = insertedIds[1],
                        epochDay = today,
                        isCompleted = true
                    )
                )
            }

            dao.insertLogs(logs)
        }
    }
}
