package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.HabitTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun DateStripSelector(
    selectedDate: LocalDate,
    onSelectDate: (LocalDate) -> Unit,
    completionMap: Map<Long, Boolean> = emptyMap(),
    modifier: Modifier = Modifier
) {
    val today = remember { LocalDate.now() }
    // Generate dates: 25 days before today to 10 days after today
    val dates = remember {
        (-25..10).map { offset -> today.plusDays(offset.toLong()) }
    }

    val listState = rememberLazyListState()

    // Scroll to center selected date on initial composition
    LaunchedEffect(Unit) {
        val index = dates.indexOf(selectedDate)
        if (index >= 0) {
            listState.scrollToItem((index - 2).coerceAtLeast(0))
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val monthYearFormat = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
            Text(
                text = selectedDate.format(monthYearFormat),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            if (selectedDate != today) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                        .clickable { onSelectDate(today) }
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CalendarToday,
                        contentDescription = "Jump to Today",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Today",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("date_strip_selector")
        ) {
            items(dates, key = { it.toEpochDay() }) { date ->
                val isSelected = date == selectedDate
                val isToday = date == today
                val hasCompleted = completionMap[date.toEpochDay()] == true

                DateStripItem(
                    date = date,
                    isSelected = isSelected,
                    isToday = isToday,
                    hasCompleted = hasCompleted,
                    onClick = { onSelectDate(date) }
                )
            }
        }
    }
}

@Composable
private fun DateStripItem(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    hasCompleted: Boolean,
    onClick: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val subtleBorder = HabitTheme.customColors.subtleBorder
    val secondaryText = HabitTheme.customColors.textSecondary
    val successColor = HabitTheme.customColors.success

    val containerBg by animateColorAsState(
        targetValue = if (isSelected) primaryColor else surfaceColor,
        label = "item_bg"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
        label = "item_text"
    )

    val subTextColor by animateColorAsState(
        targetValue = if (isSelected) Color.White.copy(alpha = 0.8f) else secondaryText,
        label = "item_subtext"
    )

    val borderColor = when {
        isSelected -> primaryColor
        isToday -> primaryColor.copy(alpha = 0.5f)
        else -> subtleBorder
    }

    Box(
        modifier = Modifier
            .width(52.dp)
            .height(72.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(containerBg)
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag("date_item_${date}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Day name (e.g., "Mon", "Tue")
            Text(
                text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                ),
                color = subTextColor
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Day number (e.g., "8")
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                ),
                color = textColor
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Status dot indicator
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isSelected -> Color.White.copy(alpha = if (hasCompleted) 1f else 0.3f)
                            hasCompleted -> successColor
                            isToday -> primaryColor.copy(alpha = 0.6f)
                            else -> Color.Transparent
                        }
                    )
            )
        }
    }
}
