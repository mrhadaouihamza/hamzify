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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.HabitEntity
import com.example.ui.theme.HabitTheme

@Composable
fun HabitCard(
    habit: HabitEntity,
    isCompleted: Boolean,
    streak: Int,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSkipped: Boolean = false,
    isOverdue: Boolean = false,
    showScheduleBadge: Boolean = true
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val subtleBorder = HabitTheme.customColors.subtleBorder
    val secondaryText = HabitTheme.customColors.textSecondary
    val habitColor = try {
        Color(android.graphics.Color.parseColor(habit.colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    // Dynamic background and border based on state
    val cardBackground = when {
        isCompleted -> surfaceColor
        isSkipped -> Color(0xFFFF5D8F).copy(alpha = 0.05f)
        isOverdue -> Color(0xFFE63946).copy(alpha = 0.04f)
        else -> surfaceColor
    }

    val cardBorderColor = when {
        isCompleted -> habitColor.copy(alpha = 0.3f)
        isSkipped -> Color(0xFFFF5D8F).copy(alpha = 0.35f)
        isOverdue -> Color(0xFFE63946).copy(alpha = 0.35f)
        else -> subtleBorder
    }

    val contentAlpha = when {
        isCompleted -> 0.72f
        isSkipped -> 0.85f
        else -> 1.0f
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(cardBackground)
            .border(
                width = 1.dp,
                color = cardBorderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag("habit_card_${habit.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon Badge
            HabitIconBadge(
                iconName = habit.iconName,
                colorHex = if (isSkipped) "#FF5D8F" else habit.colorHex,
                modifier = Modifier.size(46.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            // Title and Metadata Info (Category removed)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .alpha(contentAlpha)
            ) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Overdue Badge
                    if (isOverdue) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFE63946).copy(alpha = 0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Schedule,
                                contentDescription = "Overdue",
                                tint = Color(0xFFE63946),
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Overdue",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    lineHeight = 11.sp
                                ),
                                color = Color(0xFFE63946)
                            )
                        }
                    }

                    // Skipped Badge
                    if (isSkipped) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFFF5D8F).copy(alpha = 0.14f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.FastForward,
                                contentDescription = "Skipped",
                                tint = Color(0xFFFF5D8F),
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Skipped",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    lineHeight = 11.sp
                                ),
                                color = Color(0xFFFF5D8F)
                            )
                        }
                    }

                    // Reminder Time (if set)
                    if (showScheduleBadge && habit.reminderTime != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.AccessTime,
                                contentDescription = null,
                                tint = secondaryText,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = habit.reminderTime,
                                style = MaterialTheme.typography.labelSmall,
                                color = secondaryText
                            )
                        }
                    }

                    // Frequency Pill (if no reminder time or in addition)
                    if (habit.reminderTime == null && !isOverdue && !isSkipped) {
                        val freqText = when (habit.frequencyType) {
                            "DAILY" -> "Daily"
                            "WEEKDAYS" -> "Weekdays"
                            "WEEKENDS" -> "Weekends"
                            else -> "Custom"
                        }
                        Text(
                            text = freqText,
                            style = MaterialTheme.typography.labelSmall,
                            color = secondaryText
                        )
                    }

                    // Streak Badge (Flame icon + streak count, beautifully aligned)
                    if (streak > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(7.dp))
                                .background(Color(0xFFFF9F1C).copy(alpha = 0.14f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.LocalFireDepartment,
                                contentDescription = "Streak",
                                tint = Color(0xFFFF9F1C),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${streak}d",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    lineHeight = 12.sp
                                ),
                                color = Color(0xFFFF9F1C)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Completion Checkbox or Skipped Action Indicator
            CompletionCheckbox(
                isCompleted = isCompleted,
                onToggle = onToggle,
                activeColor = if (isSkipped) Color(0xFFFF5D8F) else habitColor
            )
        }
    }
}
