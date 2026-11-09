package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.HabitEntity
import com.example.data.local.HabitLogEntity
import com.example.data.model.calculateCurrentStreak
import com.example.data.model.isScheduledOn
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("HabitFlow", appName)
  }

  @Test
  fun `verify daily habit scheduling and streak calculation`() {
    val today = LocalDate.now()
    val habit = HabitEntity(
      id = 1L,
      name = "Daily Reading",
      frequencyType = "DAILY",
      startDateEpochDay = today.minusDays(10).toEpochDay()
    )

    assertTrue(habit.isScheduledOn(today))

    val logs = listOf(
      HabitLogEntity(habitId = 1L, epochDay = today.toEpochDay(), isCompleted = true),
      HabitLogEntity(habitId = 1L, epochDay = today.minusDays(1).toEpochDay(), isCompleted = true),
      HabitLogEntity(habitId = 1L, epochDay = today.minusDays(2).toEpochDay(), isCompleted = true)
    )

    val streak = calculateCurrentStreak(habit, logs, today)
    assertEquals(3, streak)
  }

  @Test
  fun `verify skipped habit protects streak from breaking`() {
    val today = LocalDate.now()
    val habit = HabitEntity(
      id = 2L,
      name = "Morning Meditation",
      frequencyType = "DAILY",
      startDateEpochDay = today.minusDays(10).toEpochDay()
    )

    // Today is completed, yesterday was skipped, 2 days ago was completed, 3 days ago was completed
    val logs = listOf(
      HabitLogEntity(habitId = 2L, epochDay = today.toEpochDay(), isCompleted = true, isSkipped = false),
      HabitLogEntity(habitId = 2L, epochDay = today.minusDays(1).toEpochDay(), isCompleted = false, isSkipped = true),
      HabitLogEntity(habitId = 2L, epochDay = today.minusDays(2).toEpochDay(), isCompleted = true, isSkipped = false),
      HabitLogEntity(habitId = 2L, epochDay = today.minusDays(3).toEpochDay(), isCompleted = true, isSkipped = false)
    )

    val streak = calculateCurrentStreak(habit, logs, today)
    // 3 completed days with 1 skipped day bridging without breaking streak = 3 days streak!
    assertEquals(3, streak)
  }
}
