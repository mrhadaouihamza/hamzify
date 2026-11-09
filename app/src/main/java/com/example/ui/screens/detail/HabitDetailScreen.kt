package com.example.ui.screens.detail

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.HabitEntity
import com.example.data.local.HabitLogEntity
import com.example.data.model.calculateCurrentStreak
import com.example.data.model.calculateLongestStreak
import com.example.data.model.isScheduledOn
import com.example.ui.components.HabitIconBadge
import com.example.ui.theme.HabitTheme
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

// Evaluation status enum
enum class HabitPerformanceStatus {
    EXCELLENT,
    MODERATE,
    POOR
}

@Composable
fun HabitDetailScreen(
    habit: HabitEntity,
    logs: List<HabitLogEntity>,
    selectedDate: LocalDate,
    onBack: () -> Unit,
    onEditHabit: (HabitEntity) -> Unit,
    onToggleToday: (Long) -> Unit = {},
    onDeleteHabit: ((Long) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // Intercept hardware and gesture back navigation
    BackHandler {
        onBack()
    }

    val today = remember { LocalDate.now() }

    val habitColor = remember(habit.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(habit.colorHex))
        } catch (e: Exception) {
            Color(0xFF6C63FF)
        }
    }

    val completedEpochDays = remember(logs, habit.id) {
        logs.filter { it.habitId == habit.id && it.isCompleted }.map { it.epochDay }.toSet()
    }

    val skippedEpochDays = remember(logs, habit.id) {
        logs.filter { it.habitId == habit.id && it.isSkipped }.map { it.epochDay }.toSet()
    }

    // 1. Current streak (protected against skipped days)
    val currentStreak = remember(habit, logs) {
        calculateCurrentStreak(habit, logs, today)
    }

    // 2. Best record / longest streak (protected against skipped days)
    val longestStreak = remember(habit, logs) {
        calculateLongestStreak(habit, logs)
    }

    // 3. Total times done
    val totalTimesDone = remember(completedEpochDays) {
        completedEpochDays.size
    }

    // Total times skipped
    val totalTimesSkipped = remember(skippedEpochDays) {
        skippedEpochDays.size
    }

    // 4. Days since started
    val startLocalDate = remember(habit.startDateEpochDay) {
        LocalDate.ofEpochDay(habit.startDateEpochDay)
    }
    val totalDaysElapsed = remember(habit.startDateEpochDay, today) {
        (today.toEpochDay() - habit.startDateEpochDay).coerceAtLeast(0) + 1
    }

    // 5. Total missed times: scheduled days from start date to yesterday where NOT completed AND NOT skipped!
    val totalMissedTimes = remember(habit, completedEpochDays, skippedEpochDays, today) {
        var missed = 0
        var checkDate = startLocalDate
        val yesterday = today.minusDays(1)
        while (!checkDate.isAfter(yesterday)) {
            val dateEpoch = checkDate.toEpochDay()
            if (habit.isScheduledOn(checkDate) &&
                !completedEpochDays.contains(dateEpoch) &&
                !skippedEpochDays.contains(dateEpoch)
            ) {
                missed++
            }
            checkDate = checkDate.plusDays(1)
        }
        missed
    }

    // Dynamic historical completion percentage score (Skipped days do not penalize the user!)
    val (scorePercentage, performanceStatus) = remember(habit, totalTimesDone, totalMissedTimes) {
        val totalScheduledOpportunities = (totalTimesDone + totalMissedTimes).coerceAtLeast(1)
        val score = ((totalTimesDone.toFloat() / totalScheduledOpportunities.toFloat()) * 100).toInt().coerceIn(0, 100)
        val status = when {
            score >= 75 -> HabitPerformanceStatus.EXCELLENT
            score >= 45 -> HabitPerformanceStatus.MODERATE
            else -> HabitPerformanceStatus.POOR
        }
        score to status
    }

    // Month navigator state for the monthly calendar grid view
    var currentMonth by remember { mutableStateOf(YearMonth.from(selectedDate)) }

    // Dynamic Theme Color Tokens (Automatically adapts to Light and Dark mode)
    val screenBg = MaterialTheme.colorScheme.background
    val cardBg = MaterialTheme.colorScheme.surface
    val cardBorder = HabitTheme.customColors.subtleBorder
    val textColor = MaterialTheme.colorScheme.onSurface
    val secondaryText = HabitTheme.customColors.textSecondary
    val surfaceSubtle = HabitTheme.customColors.surfaceSubtle

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(screenBg)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("habit_detail_screen_content"),
            contentPadding = PaddingValues(bottom = 60.dp)
        ) {
            // Sleek Top App Bar: Back Button (Left), Title (Center), Edit Button (Right)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(cardBg)
                            .border(1.dp, cardBorder, CircleShape)
                            .testTag("habit_detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = textColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = "Habit Analytics",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        color = textColor
                    )

                    IconButton(
                        onClick = { onEditHabit(habit) },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(cardBg)
                            .border(1.dp, cardBorder, CircleShape)
                            .testTag("habit_detail_edit_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Edit Habit",
                            tint = habitColor,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }

            // Habit Profile Header Card (Category and redundant completion button completely removed)
            item {
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .background(cardBg)
                            .border(1.dp, cardBorder, RoundedCornerShape(22.dp))
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HabitIconBadge(
                                iconName = habit.iconName,
                                colorHex = habit.colorHex,
                                size = 56.dp,
                                iconSize = 30.dp,
                                shapeRadius = 18.dp
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = habit.name,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    ),
                                    color = textColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(3.dp))

                                Text(
                                    text = formatFrequencyLabel(habit),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = secondaryText
                                )

                                if (habit.reminderTime != null) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Daily Reminder: ${habit.reminderTime}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = habitColor
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // 1. Top Analytical Banner & Dynamic Status Indicator
            item {
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                    AnalyticalStatusBanner(
                        score = scorePercentage,
                        status = performanceStatus,
                        cardBg = cardBg,
                        cardBorder = cardBorder,
                        textColor = textColor,
                        secondaryText = secondaryText
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Section: Consistency Records Header
            item {
                Text(
                    text = "Consistency Records",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    ),
                    color = textColor,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // 2. Consistency Records & Statistical Cards
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Row 1: Current Streak & Best Streak
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = "Current Streak",
                            value = "$currentStreak Days",
                            subtitle = if (currentStreak > 0) "Ongoing consistency" else "Completed today?",
                            icon = Icons.Rounded.LocalFireDepartment,
                            iconTint = Color(0xFFFF9F1C),
                            cardBg = cardBg,
                            cardBorder = cardBorder,
                            textColor = textColor,
                            secondaryText = secondaryText,
                            modifier = Modifier.weight(1f)
                        )

                        StatCard(
                            title = "Best Record",
                            value = "$longestStreak Days",
                            subtitle = "Longest unbroken streak",
                            icon = Icons.Rounded.EmojiEvents,
                            iconTint = Color(0xFFFFD166),
                            cardBg = cardBg,
                            cardBorder = cardBorder,
                            textColor = textColor,
                            secondaryText = secondaryText,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Row 2: Total Times Done & Total Missed Times
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = "Total Times Done",
                            value = "$totalTimesDone Times",
                            subtitle = "Cumulative completed",
                            icon = Icons.Rounded.CheckCircle,
                            iconTint = HabitTheme.customColors.success,
                            cardBg = cardBg,
                            cardBorder = cardBorder,
                            textColor = textColor,
                            secondaryText = secondaryText,
                            modifier = Modifier.weight(1f)
                        )

                        StatCard(
                            title = "Total Missed Times",
                            value = "$totalMissedTimes Missed",
                            subtitle = if (totalTimesSkipped > 0) "$totalTimesSkipped excused / skipped" else "Uncompleted past days",
                            icon = Icons.Rounded.Close,
                            iconTint = Color(0xFFFF5D8F),
                            cardBg = cardBg,
                            cardBorder = cardBorder,
                            textColor = textColor,
                            secondaryText = secondaryText,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Row 3: Days Since Started
                    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault()) }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(cardBg)
                            .border(1.dp, cardBorder, RoundedCornerShape(18.dp))
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(habitColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.DateRange,
                                    contentDescription = null,
                                    tint = habitColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Days Since Started",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = secondaryText
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$totalDaysElapsed Days Elapsed",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp
                                    ),
                                    color = textColor
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Started on ${startLocalDate.format(dateFormatter)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = secondaryText
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // 3. Monthly Tracking Calendar (Full-Month Grid View with Skipped Day Support)
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Monthly Tracking Calendar",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            ),
                            color = textColor
                        )

                        // Month Navigator: < Month YYYY >
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { currentMonth = currentMonth.minusMonths(1) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ChevronLeft,
                                    contentDescription = "Previous Month",
                                    tint = secondaryText
                                )
                            }

                            val monthFormatter = remember { DateTimeFormatter.ofPattern("MMM yyyy", Locale.getDefault()) }
                            Text(
                                text = currentMonth.format(monthFormatter),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = textColor
                            )

                            IconButton(
                                onClick = { currentMonth = currentMonth.plusMonths(1) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ChevronRight,
                                    contentDescription = "Next Month",
                                    tint = secondaryText
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Fixed Monthly Grid Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .background(cardBg)
                            .border(1.dp, cardBorder, RoundedCornerShape(22.dp))
                            .padding(18.dp)
                    ) {
                        HabitMonthlyGrid(
                            yearMonth = currentMonth,
                            completedDays = completedEpochDays,
                            skippedDays = skippedEpochDays,
                            habit = habit,
                            habitColor = habitColor,
                            cardBorder = cardBorder,
                            textColor = textColor,
                            secondaryText = secondaryText,
                            surfaceSubtle = surfaceSubtle
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Bottom Actions: Edit Settings & Delete
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { onEditHabit(habit) },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = textColor
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("detail_edit_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = null,
                            tint = habitColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Edit Habit Settings",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = textColor
                        )
                    }

                    if (onDeleteHabit != null) {
                        Button(
                            onClick = {
                                onDeleteHabit(habit.id)
                                onBack()
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Delete Habit",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 1. Top Analytical Banner & Dynamic Status Indicator
 */
@Composable
private fun AnalyticalStatusBanner(
    score: Int,
    status: HabitPerformanceStatus,
    cardBg: Color,
    cardBorder: Color,
    textColor: Color,
    secondaryText: Color
) {
    val (statusTitle, statusRating, statusMessage, statusColor, statusIcon) = when (status) {
        HabitPerformanceStatus.EXCELLENT -> {
            Tuple5(
                "Excellent Status",
                "Amazing performance! 🎉",
                "Congratulation! You are crushing this habit with outstanding consistency. Keep this incredible momentum going!",
                Color(0xFF38B779), // Vibrant green
                Icons.Rounded.Verified
            )
        }
        HabitPerformanceStatus.MODERATE -> {
            Tuple5(
                "Moderate Status",
                "Pay attention to consistency ⚠️",
                "You are maintaining progress, but missed days are starting to pile up. A little more focus will get you fully back on track!",
                Color(0xFFFF9F1C), // Warm amber
                Icons.Rounded.Warning
            )
        }
        HabitPerformanceStatus.POOR -> {
            Tuple5(
                "Deteriorating Status",
                "Habit needs immediate intervention! 🚨",
                "Alert: This habit is currently failing. Rebuild your streak by taking an effortless micro-action today to regain momentum!",
                Color(0xFFFF5D8F), // Danger pink/coral
                Icons.Rounded.ErrorOutline
            )
        }
    }

    val animatedScore by animateFloatAsState(
        targetValue = score / 100f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "score_progress"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(cardBg)
            .border(1.5.dp, statusColor.copy(alpha = 0.35f), RoundedCornerShape(22.dp))
            .padding(20.dp)
            .testTag("habit_analytical_banner")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(statusColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = statusTitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = statusColor
                    )
                }

                // Score pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$score% Score",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Subtitle
            Text(
                text = statusRating,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = textColor
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Detailed analytical evaluation message
            Text(
                text = statusMessage,
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                color = secondaryText
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Linear Progress Meter
            LinearProgressIndicator(
                progress = { animatedScore },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = statusColor,
                trackColor = statusColor.copy(alpha = 0.15f)
            )
        }
    }
}

/**
 * 2. Stat Metric Card Component
 */
@Composable
private fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    cardBg: Color,
    cardBorder: Color,
    textColor: Color,
    secondaryText: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(18.dp))
            .padding(16.dp)
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
                    color = secondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                color = textColor
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = secondaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * 3. Fixed Monthly Tracking Calendar Grid (Displays full 28, 29, 30, or 31 days aligned Mon-Sun)
 */
@Composable
private fun HabitMonthlyGrid(
    yearMonth: YearMonth,
    completedDays: Set<Long>,
    skippedDays: Set<Long>,
    habit: HabitEntity,
    habitColor: Color,
    cardBorder: Color,
    textColor: Color,
    secondaryText: Color,
    surfaceSubtle: Color
) {
    val today = remember { LocalDate.now() }
    val daysInMonth = yearMonth.lengthOfMonth() // Exactly 28, 29, 30, or 31 days
    val firstDayOfWeek = yearMonth.atDay(1).dayOfWeek.value // 1 (Mon) to 7 (Sun)

    val dayHeaders = listOf("M", "T", "W", "T", "F", "S", "S")

    Column(modifier = Modifier.fillMaxWidth()) {
        // Weekday headers row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            dayHeaders.forEach { header ->
                Text(
                    text = header,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    ),
                    color = secondaryText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Total slots = leading empty days (firstDayOfWeek - 1) + all days in this specific month
        val leadingEmptySlots = firstDayOfWeek - 1
        val totalSlots = leadingEmptySlots + daysInMonth
        val rows = (totalSlots + 6) / 7

        for (row in 0 until rows) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (col in 1..7) {
                    val slotIndex = row * 7 + col
                    val dayNumber = slotIndex - leadingEmptySlots

                    if (dayNumber in 1..daysInMonth) {
                        val date = yearMonth.atDay(dayNumber)
                        val dateEpochDay = date.toEpochDay()
                        val isCompleted = completedDays.contains(dateEpochDay)
                        val isSkipped = skippedDays.contains(dateEpochDay)
                        val isToday = date == today
                        val isPast = date.isBefore(today)
                        val isScheduled = habit.isScheduledOn(date)

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .then(
                                    when {
                                        isCompleted -> Modifier.background(habitColor)
                                        isSkipped -> Modifier
                                            .border(1.5.dp, Color(0xFFFF5D8F), CircleShape)
                                            .background(Color(0xFFFF5D8F).copy(alpha = 0.2f))
                                        isToday -> Modifier
                                            .border(1.5.dp, habitColor, CircleShape)
                                            .background(habitColor.copy(alpha = 0.12f))
                                        isPast && isScheduled -> Modifier
                                            .border(1.dp, cardBorder, CircleShape)
                                            .background(surfaceSubtle)
                                        else -> Modifier
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayNumber.toString(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isCompleted || isSkipped || isToday) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp
                                ),
                                color = when {
                                    isCompleted -> Color.White
                                    isSkipped -> Color(0xFFFF5D8F)
                                    isToday -> habitColor
                                    isPast && isScheduled -> secondaryText
                                    date.isAfter(today) -> secondaryText.copy(alpha = 0.4f)
                                    else -> textColor.copy(alpha = 0.7f)
                                }
                            )
                        }
                    } else {
                        // Empty slot outside the month range
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
            // Completed
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(habitColor)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = "Done",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = secondaryText
            )

            Spacer(modifier = Modifier.width(14.dp))

            // Skipped
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF5D8F).copy(alpha = 0.25f))
                    .border(1.dp, Color(0xFFFF5D8F), CircleShape)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = "Skipped",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = secondaryText
            )

            Spacer(modifier = Modifier.width(14.dp))

            // Missed
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(surfaceSubtle)
                    .border(1.dp, cardBorder, CircleShape)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = "Missed",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = secondaryText
            )

            Spacer(modifier = Modifier.width(14.dp))

            // Today
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .border(1.dp, habitColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = "Today",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = secondaryText
            )
        }
    }
}

private fun formatFrequencyLabel(habit: HabitEntity): String {
    return when (habit.frequencyType) {
        "DAILY" -> "Every Day"
        "WEEKDAYS" -> "Weekdays"
        "WEEKENDS" -> "Weekends"
        else -> "Custom Days"
    }
}

// Simple internal helper class for 5-tuple
private data class Tuple5<A, B, C, D, E>(
    val a: A,
    val b: B,
    val c: C,
    val d: D,
    val e: E
)
