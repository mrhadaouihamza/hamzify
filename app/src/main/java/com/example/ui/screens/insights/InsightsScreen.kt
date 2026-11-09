package com.example.ui.screens.insights

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.HabitIconBadge
import com.example.ui.theme.HabitTheme
import java.time.LocalDate

@Composable
fun InsightsScreen(
    viewModel: InsightsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("insights_screen_list"),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = "Insights & Analytics",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Behavioral consistency and streak statistics",
                        style = MaterialTheme.typography.bodyMedium,
                        color = HabitTheme.customColors.textSecondary
                    )
                }
            }

            // Key Metrics 2x2 Grid
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricCard(
                            title = "Active Streak",
                            value = "${uiState.currentStreak} Days",
                            subtitle = "Best record: ${uiState.longestStreak}d",
                            icon = Icons.Rounded.LocalFireDepartment,
                            accentColor = Color(0xFFFF9F1C),
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "Completion Rate",
                            value = "${uiState.overallCompletionRate}%",
                            subtitle = "Last 30 days",
                            icon = Icons.Rounded.TrendingUp,
                            accentColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricCard(
                            title = "Longest Streak",
                            value = "${uiState.longestStreak} Days",
                            subtitle = "Personal milestone",
                            icon = Icons.Rounded.EmojiEvents,
                            accentColor = Color(0xFFFFD166),
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "Total Check-ins",
                            value = "${uiState.totalCompletionsAllTime}",
                            subtitle = "All-time actions",
                            icon = Icons.Rounded.CheckCircle,
                            accentColor = HabitTheme.customColors.success,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Weekly Completion Bar Chart
            item {
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    WeeklyBarChartCard(weeklyStats = uiState.weeklyStats)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Monthly Consistency Heatmap
            item {
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    MonthlyHeatmapCard(heatmapDays = uiState.monthlyHeatmap)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Behavioral Insight Banner
            if (uiState.topPerformingHabitName != null) {
                item {
                    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Psychology,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = "Top Consistency",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Your most reliable habit is \"${uiState.topPerformingHabitName}\". Consistent morning momentum compounds quickly!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // Habit Performance Ranking Section Header
            item {
                Text(
                    text = "Habit Performance",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Habit Performance Items
            items(uiState.habitPerformances, key = { it.habit.id }) { item ->
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                    HabitPerformanceCard(item = item)
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val subtleBorder = HabitTheme.customColors.subtleBorder
    val secondaryText = HabitTheme.customColors.textSecondary

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(surfaceColor)
            .border(1.dp, subtleBorder, RoundedCornerShape(20.dp))
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
                    style = MaterialTheme.typography.labelMedium,
                    color = secondaryText
                )

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = secondaryText
            )
        }
    }
}

@Composable
private fun WeeklyBarChartCard(
    weeklyStats: List<DayCompletionStat>
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val subtleBorder = HabitTheme.customColors.subtleBorder
    val primaryColor = MaterialTheme.colorScheme.primary
    val today = LocalDate.now()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(surfaceColor)
            .border(1.dp, subtleBorder, RoundedCornerShape(22.dp))
            .padding(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Weekly Completion",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Past 7 Days",
                    style = MaterialTheme.typography.labelSmall,
                    color = HabitTheme.customColors.textSecondary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bars Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                weeklyStats.forEach { stat ->
                    val isToday = stat.date == today
                    val animatedHeight by animateFloatAsState(
                        targetValue = stat.completionPercentage,
                        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
                        label = "bar_anim_${stat.date}"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Percent text above bar
                        Text(
                            text = "${(stat.completionPercentage * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = if (isToday) primaryColor else HabitTheme.customColors.textSecondary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Vertical Bar Container
                        Box(
                            modifier = Modifier
                                .width(22.dp)
                                .height(90.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(HabitTheme.customColors.surfaceSubtle),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(fraction = animatedHeight.coerceAtLeast(0.08f))
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isToday) primaryColor
                                        else HabitTheme.customColors.success
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Day label (e.g. "M", "T")
                        Text(
                            text = stat.dayLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isToday) primaryColor else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthlyHeatmapCard(
    heatmapDays: List<HeatmapDay>
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val subtleBorder = HabitTheme.customColors.subtleBorder
    val secondaryText = HabitTheme.customColors.textSecondary
    val successColor = HabitTheme.customColors.success

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(surfaceColor)
            .border(1.dp, subtleBorder, RoundedCornerShape(22.dp))
            .padding(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Monthly Consistency",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "4-Week Heatmap",
                    style = MaterialTheme.typography.labelSmall,
                    color = secondaryText
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 4 rows of 7 days
            val weeks = heatmapDays.chunked(7)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                weeks.forEach { week ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        week.forEach { day ->
                            val cellColor = when (day.intensityLevel) {
                                3 -> successColor
                                2 -> successColor.copy(alpha = 0.65f)
                                1 -> successColor.copy(alpha = 0.32f)
                                else -> HabitTheme.customColors.surfaceSubtle
                            }

                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(cellColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day.date.dayOfMonth.toString(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = if (day.intensityLevel >= 2) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Legend Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Less",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = secondaryText
                )
                Spacer(modifier = Modifier.width(6.dp))
                listOf(
                    HabitTheme.customColors.surfaceSubtle,
                    successColor.copy(alpha = 0.32f),
                    successColor.copy(alpha = 0.65f),
                    successColor
                ).forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(color)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "More",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = secondaryText
                )
            }
        }
    }
}

@Composable
private fun HabitPerformanceCard(
    item: HabitPerformanceItem
) {
    val habit = item.habit
    val surfaceColor = MaterialTheme.colorScheme.surface
    val subtleBorder = HabitTheme.customColors.subtleBorder
    val secondaryText = HabitTheme.customColors.textSecondary
    val habitColor = try {
        Color(android.graphics.Color.parseColor(habit.colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(surfaceColor)
            .border(1.dp, subtleBorder, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    HabitIconBadge(
                        iconName = habit.iconName,
                        colorHex = habit.colorHex,
                        modifier = Modifier.size(42.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = habit.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${item.currentStreak}d current streak",
                            style = MaterialTheme.typography.labelSmall,
                            color = secondaryText
                        )
                    }
                }

                Text(
                    text = "${item.completionRate30d}%",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = habitColor
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Animated Linear Progress Bar
            val animatedProgress by animateFloatAsState(
                targetValue = (item.completionRate30d / 100f).coerceIn(0f, 1f),
                animationSpec = tween(600),
                label = "linear_prog_${habit.id}"
            )

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = habitColor,
                trackColor = habitColor.copy(alpha = 0.15f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Details footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Current streak: ${item.currentStreak}d",
                    style = MaterialTheme.typography.labelSmall,
                    color = secondaryText
                )
                Text(
                    text = "Best streak: ${item.longestStreak}d",
                    style = MaterialTheme.typography.labelSmall,
                    color = secondaryText
                )
                Text(
                    text = "${item.totalCompletions} all-time",
                    style = MaterialTheme.typography.labelSmall,
                    color = secondaryText
                )
            }
        }
    }
}
