package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.HabitEntity
import com.example.data.local.HabitLogEntity
import com.example.data.model.calculateCurrentStreak
import com.example.data.model.calculateLongestStreak
import com.example.data.model.isScheduledOn
import com.example.ui.theme.HabitTheme
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailSheet(
    habit: HabitEntity,
    logs: List<HabitLogEntity>,
    selectedDate: LocalDate,
    onDismiss: () -> Unit,
    onToggleToday: (Long) -> Unit,
    onEditHabit: (HabitEntity) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val today = remember { LocalDate.now() }

    val habitColor = remember(habit.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(habit.colorHex))
        } catch (e: Exception) {
            Color(0xFF6C63FF)
        }
    }

    val habitLogs = remember(logs, habit.id) {
        logs.filter { it.habitId == habit.id && it.isCompleted }
    }

    val completedEpochDays = remember(habitLogs) {
        habitLogs.map { it.epochDay }.toSet()
    }

    val currentStreak = remember(habit, logs) {
        calculateCurrentStreak(habit, logs, today)
    }

    val longestStreak = remember(habit, logs) {
        calculateLongestStreak(habit, logs)
    }

    val totalCompletions = remember(habitLogs) {
        habitLogs.size
    }

    // 30-day rate
    val completionRate30d = remember(habit, completedEpochDays) {
        val thirtyDaysAgo = today.minusDays(29).toEpochDay()
        val scheduledCount = (0..29).count { offset -> habit.isScheduledOn(today.minusDays(offset.toLong())) }
        val completedCount = habitLogs.count { it.epochDay in thirtyDaysAgo..today.toEpochDay() }
        if (scheduledCount > 0) ((completedCount.toFloat() / scheduledCount.toFloat()) * 100).toInt().coerceIn(0, 100) else 0
    }

    val isCompletedOnSelectedDate = completedEpochDays.contains(selectedDate.toEpochDay())

    // Month navigator for the monthly calendar grid view
    var currentMonth by remember { mutableStateOf(YearMonth.from(selectedDate)) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null,
        modifier = Modifier.testTag("habit_detail_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 20.dp, bottom = 28.dp)
                .navigationBarsPadding()
        ) {
            // Top Nav Row: Close & Edit Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = HabitTheme.customColors.textSecondary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            onDismiss()
                            onEditHabit(habit)
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Edit Habit",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Habit Profile Header
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HabitIconBadge(
                    iconName = habit.iconName,
                    colorHex = habit.colorHex,
                    size = 64.dp,
                    iconSize = 34.dp,
                    shapeRadius = 20.dp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = formatFrequency(habit),
                    style = MaterialTheme.typography.bodyMedium,
                    color = HabitTheme.customColors.textSecondary
                )

                if (habit.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = habit.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Today Status Action Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(habitColor.copy(alpha = 0.1f))
                    .border(1.dp, habitColor.copy(alpha = 0.25f), RoundedCornerShape(18.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isCompletedOnSelectedDate) Icons.Rounded.CheckCircle else Icons.Rounded.AccessTime,
                            contentDescription = null,
                            tint = if (isCompletedOnSelectedDate) HabitTheme.customColors.success else habitColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isCompletedOnSelectedDate) "Completed for selected day" else "Pending for selected day",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Button(
                        onClick = { onToggleToday(habit.id) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCompletedOnSelectedDate) HabitTheme.customColors.surfaceSubtle else habitColor,
                            contentColor = if (isCompletedOnSelectedDate) MaterialTheme.colorScheme.onSurface else Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("detail_toggle_today_button")
                    ) {
                        Text(
                            text = if (isCompletedOnSelectedDate) "Undo" else "Done",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Statistics 2x2 Grid
            Text(
                text = "Consistency Records",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DetailMetricCard(
                    title = "Current Streak",
                    value = "$currentStreak Days",
                    icon = Icons.Rounded.LocalFireDepartment,
                    tint = Color(0xFFFF9F1C),
                    modifier = Modifier.weight(1f)
                )
                DetailMetricCard(
                    title = "Best Record",
                    value = "$longestStreak Days",
                    icon = Icons.Rounded.EmojiEvents,
                    tint = Color(0xFFFFD166),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DetailMetricCard(
                    title = "Total Completed",
                    value = "$totalCompletions Times",
                    icon = Icons.Rounded.CheckCircle,
                    tint = HabitTheme.customColors.success,
                    modifier = Modifier.weight(1f)
                )
                DetailMetricCard(
                    title = "30-Day Rate",
                    value = "$completionRate30d%",
                    icon = Icons.Rounded.TrendingUp,
                    tint = habitColor,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Monthly Calendar Grid View
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Monthly Tracking",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Month switcher (< Month YYYY >)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { currentMonth = currentMonth.minusMonths(1) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ChevronLeft,
                            contentDescription = "Previous Month",
                            tint = HabitTheme.customColors.textSecondary
                        )
                    }

                    val monthTitleFormatter = remember { DateTimeFormatter.ofPattern("MMM yyyy", Locale.getDefault()) }
                    Text(
                        text = currentMonth.format(monthTitleFormatter),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    IconButton(
                        onClick = { currentMonth = currentMonth.plusMonths(1) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = "Next Month",
                            tint = HabitTheme.customColors.textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Calendar Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, HabitTheme.customColors.subtleBorder, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                MonthlyCalendarGrid(
                    yearMonth = currentMonth,
                    completedDays = completedEpochDays,
                    habit = habit,
                    habitColor = habitColor
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Action: Edit Habit Button
            OutlinedButton(
                onClick = {
                    onDismiss()
                    onEditHabit(habit)
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Edit Habit Settings",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}

@Composable
private fun DetailMetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, HabitTheme.customColors.subtleBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = HabitTheme.customColors.textSecondary
                )
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(tint.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun MonthlyCalendarGrid(
    yearMonth: YearMonth,
    completedDays: Set<Long>,
    habit: HabitEntity,
    habitColor: Color
) {
    val today = remember { LocalDate.now() }
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfWeek = yearMonth.atDay(1).dayOfWeek.value // 1 (Mon) - 7 (Sun)

    val dayHeaders = listOf("M", "T", "W", "T", "F", "S", "S")

    Column(modifier = Modifier.fillMaxWidth()) {
        // Weekday header letters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            dayHeaders.forEach { header ->
                Text(
                    text = header,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    ),
                    color = HabitTheme.customColors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Calendar Days Grid (rows of 7)
        val totalSlots = (firstDayOfWeek - 1) + daysInMonth
        val rows = (totalSlots + 6) / 7

        for (row in 0 until rows) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (col in 1..7) {
                    val slotIndex = row * 7 + col
                    val dayNumber = slotIndex - (firstDayOfWeek - 1)

                    if (dayNumber in 1..daysInMonth) {
                        val date = yearMonth.atDay(dayNumber)
                        val isCompleted = completedDays.contains(date.toEpochDay())
                        val isToday = date == today
                        val isFuture = date.isAfter(today)
                        val isScheduled = habit.isScheduledOn(date)

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .then(
                                    when {
                                        isCompleted -> Modifier.background(habitColor)
                                        isToday -> Modifier
                                            .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                        isScheduled && !isFuture -> Modifier.background(HabitTheme.customColors.surfaceSubtle)
                                        else -> Modifier
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayNumber.toString(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isCompleted || isToday) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp
                                ),
                                color = when {
                                    isCompleted -> Color.White
                                    isToday -> MaterialTheme.colorScheme.primary
                                    isFuture -> HabitTheme.customColors.textSecondary.copy(alpha = 0.4f)
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    } else {
                        // Empty slot
                        Spacer(modifier = Modifier.size(36.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Legend row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(habitColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Completed",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = HabitTheme.customColors.textSecondary
            )

            Spacer(modifier = Modifier.width(18.dp))

            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Today",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = HabitTheme.customColors.textSecondary
            )
        }
    }
}

private fun formatFrequency(habit: HabitEntity): String {
    return when (habit.frequencyType) {
        "DAILY" -> "Daily"
        "WEEKDAYS" -> "Mon - Fri"
        "WEEKENDS" -> "Weekends"
        else -> "Custom days"
    }
}
