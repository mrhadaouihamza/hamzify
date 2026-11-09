package com.example.ui.screens.today

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.HabitEntity
import com.example.ui.components.AddEditHabitSheet
import com.example.ui.components.DateStripSelector
import com.example.ui.components.SwipeableHabitCard
import com.example.ui.screens.detail.HabitDetailScreen
import com.example.ui.theme.HabitTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    viewModel: TodayViewModel,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    // State for modals
    var showAddSheet by remember { mutableStateOf(false) }
    var habitToEdit by remember { mutableStateOf<HabitEntity?>(null) }
    var habitForDetail by remember { mutableStateOf<HabitEntity?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("today_screen_list"),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Prominent Top Horizontal Date Bar
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(top = 8.dp)
                ) {
                    DateStripSelector(
                        selectedDate = uiState.selectedDate,
                        onSelectDate = { viewModel.selectDate(it) },
                        completionMap = uiState.completionMap
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Top Section Header: Active / Uncompleted Habits
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Habits Routine",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "${uiState.completedCount}/${uiState.totalCount} completed",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = HabitTheme.customColors.textSecondary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // 1. Top Section: Active / Uncompleted Habits for the Day
            if (uiState.totalCount == 0) {
                item {
                    EmptyHabitsPlaceholder(
                        onAddClick = { showAddSheet = true }
                    )
                }
            } else if (uiState.uncompletedHabits.isEmpty()) {
                item {
                    AllDoneCard()
                }
            } else {
                items(
                    items = uiState.uncompletedHabits,
                    key = { it.habit.id }
                ) { item ->
                    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp)) {
                        SwipeableHabitCard(
                            habit = item.habit,
                            isCompleted = item.isCompleted,
                            isSkipped = item.isSkipped,
                            isOverdue = item.isOverdue,
                            streak = item.streak,
                            onToggle = { viewModel.toggleHabit(item.habit.id) },
                            onClick = {
                                habitForDetail = item.habit
                            },
                            onDone = {
                                viewModel.markDone(item.habit.id)
                            },
                            onSkip = {
                                viewModel.markSkip(item.habit.id)
                            }
                        )
                    }
                }
            }

            // 2. Bottom Section: Dedicated "Completed Habits" Section
            if (uiState.completedHabits.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Completed Habits",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(HabitTheme.customColors.success.copy(alpha = 0.15f))
                                    .padding(horizontal = 7.dp, vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${uiState.completedHabits.size}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    color = HabitTheme.customColors.success
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                items(
                    items = uiState.completedHabits,
                    key = { it.habit.id }
                ) { item ->
                    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp)) {
                        SwipeableHabitCard(
                            habit = item.habit,
                            isCompleted = true,
                            isSkipped = false,
                            isOverdue = false,
                            streak = item.streak,
                            onToggle = { viewModel.toggleHabit(item.habit.id) },
                            onClick = {
                                habitForDetail = item.habit
                            },
                            onDone = {
                                viewModel.markDone(item.habit.id)
                            },
                            onSkip = {
                                viewModel.markSkip(item.habit.id)
                            }
                        )
                    }
                }
            }
        }

        // Quick Add Floating Action Button
        FloatingActionButton(
            onClick = { showAddSheet = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 24.dp)
                .testTag("today_add_habit_fab")
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "Add Habit",
                modifier = Modifier.size(28.dp)
            )
        }
    }

    // Dedicated Habit Detail & Analytics Screen
    if (habitForDetail != null) {
        HabitDetailScreen(
            habit = habitForDetail!!,
            logs = uiState.allLogs,
            selectedDate = uiState.selectedDate,
            onBack = { habitForDetail = null },
            onToggleToday = { habitId ->
                viewModel.toggleHabit(habitId)
            },
            onEditHabit = { habit ->
                habitForDetail = null
                habitToEdit = habit
            },
            onDeleteHabit = { habitId ->
                viewModel.deleteHabit(habitId)
                habitForDetail = null
            }
        )
    }

    // Add / Edit Bottom Sheet
    if (showAddSheet || habitToEdit != null) {
        AddEditHabitSheet(
            habitToEdit = habitToEdit,
            onDismiss = {
                showAddSheet = false
                habitToEdit = null
            },
            onSave = { habit ->
                if (habitToEdit == null) {
                    viewModel.createHabit(habit)
                } else {
                    viewModel.updateHabit(habit)
                }
                showAddSheet = false
                habitToEdit = null
            },
            onDelete = { habitId ->
                viewModel.deleteHabit(habitId)
                habitToEdit = null
            }
        )
    }
}

@Composable
private fun AllDoneCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, HabitTheme.customColors.subtleBorder, RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(HabitTheme.customColors.success.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.TaskAlt,
                    contentDescription = null,
                    tint = HabitTheme.customColors.success,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "All Habits Completed! 🎉",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Outstanding consistency today. Keep up the great work!",
                    style = MaterialTheme.typography.bodySmall,
                    color = HabitTheme.customColors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun EmptyHabitsPlaceholder(
    onAddClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                1.dp,
                HabitTheme.customColors.subtleBorder,
                RoundedCornerShape(24.dp)
            )
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircleOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No Habits for this Day",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Add a new routine or check your frequency settings to keep your progress on track.",
                style = MaterialTheme.typography.bodyMedium,
                color = HabitTheme.customColors.textSecondary,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable(onClick = onAddClick)
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Add Habit",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
        }
    }
}
