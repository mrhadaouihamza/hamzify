package com.example.ui.screens.habits

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Unarchive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.HabitEntity
import com.example.ui.components.AddEditHabitSheet
import com.example.ui.components.HabitIconBadge
import com.example.ui.theme.HabitTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreen(
    viewModel: HabitsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
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
                .testTag("habits_screen_list"),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Habit Library",
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 26.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${uiState.habits.size} ${if (uiState.showArchived) "archived" else "active"} habits",
                            style = MaterialTheme.typography.bodyMedium,
                            color = HabitTheme.customColors.textSecondary
                        )
                    }

                    // Add Habit Button
                    Button(
                        onClick = { showAddSheet = true },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                        modifier = Modifier.testTag("habits_add_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Add Habit",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            // Search Bar
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        placeholder = { Text("Search your habits...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = "Search",
                                tint = HabitTheme.customColors.textSecondary
                            )
                        },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                    Icon(
                                        imageVector = Icons.Rounded.Clear,
                                        contentDescription = "Clear",
                                        tint = HabitTheme.customColors.textSecondary
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = HabitTheme.customColors.subtleBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("habits_search_input")
                    )
                }
            }

            // Status Filter (Active vs Archived)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (!uiState.showArchived) MaterialTheme.colorScheme.primary
                                else HabitTheme.customColors.surfaceSubtle
                            )
                            .clickable { viewModel.onToggleShowArchived(false) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Active Habits",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (!uiState.showArchived) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (!uiState.showArchived) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (uiState.showArchived) MaterialTheme.colorScheme.primary
                                else HabitTheme.customColors.surfaceSubtle
                            )
                            .clickable { viewModel.onToggleShowArchived(true) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Archived",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (uiState.showArchived) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (uiState.showArchived) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Habits List
            if (uiState.habits.isEmpty()) {
                item {
                    HabitsEmptyView(
                        isArchivedTab = uiState.showArchived,
                        isSearching = uiState.searchQuery.isNotEmpty(),
                        onAddHabit = { showAddSheet = true }
                    )
                }
            } else {
                items(
                    items = uiState.habits,
                    key = { it.habit.id }
                ) { item ->
                    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                        HabitManagementCard(
                            item = item,
                            onEdit = { habitForDetail = item.habit },
                            onToggleArchive = { viewModel.setArchived(item.habit.id, !item.habit.isArchived) },
                            onDelete = { viewModel.deleteHabit(item.habit.id) }
                        )
                    }
                }
            }
        }

        // Habit Detail & Analytics Screen
        if (habitForDetail != null) {
            com.example.ui.screens.detail.HabitDetailScreen(
                habit = habitForDetail!!,
                logs = uiState.allLogs,
                selectedDate = java.time.LocalDate.now(),
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
                    viewModel.insertHabit(habit)
                } else {
                    viewModel.updateHabit(habit)
                }
                showAddSheet = false
                habitToEdit = null
            },
            onArchiveToggle = { id, archived ->
                viewModel.setArchived(id, archived)
            },
            onDelete = { id ->
                viewModel.deleteHabit(id)
                habitToEdit = null
            }
        )
    }
}

@Composable
private fun HabitManagementCard(
    item: HabitDetailItem,
    onEdit: () -> Unit,
    onToggleArchive: () -> Unit,
    onDelete: () -> Unit
) {
    val habit = item.habit
    var menuExpanded by remember { mutableStateOf(false) }

    val surfaceColor = MaterialTheme.colorScheme.surface
    val subtleBorder = HabitTheme.customColors.subtleBorder
    val secondaryText = HabitTheme.customColors.textSecondary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(surfaceColor)
            .border(1.dp, subtleBorder, RoundedCornerShape(20.dp))
            .clickable(onClick = onEdit)
            .padding(16.dp)
            .testTag("habit_mgmt_card_${habit.id}")
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
                        modifier = Modifier.size(46.dp)
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = habit.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        val freqLabel = when (habit.frequencyType) {
                            "DAILY" -> "Every Day"
                            "WEEKDAYS" -> "Weekdays"
                            "WEEKENDS" -> "Weekends"
                            else -> "Custom Days"
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = freqLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = secondaryText
                        )
                    }
                }

                // Options Menu
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = "Actions",
                            tint = secondaryText
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            leadingIcon = {
                                Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                            },
                            onClick = {
                                menuExpanded = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (habit.isArchived) "Unarchive" else "Archive") },
                            leadingIcon = {
                                Icon(
                                    if (habit.isArchived) Icons.Rounded.Unarchive else Icons.Rounded.Archive,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onToggleArchive()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            if (habit.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = habit.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = secondaryText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Stats Pill Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Current Streak Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(HabitTheme.customColors.surfaceSubtle)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocalFireDepartment,
                        contentDescription = null,
                        tint = Color(0xFFFF9F1C),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Current: ${item.currentStreak}d",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Best Streak Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(HabitTheme.customColors.surfaceSubtle)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Best: ${item.longestStreak}d",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                if (habit.reminderTime != null) {
                    Text(
                        text = habit.reminderTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = secondaryText
                    )
                }
            }
        }
    }
}

@Composable
private fun HabitsEmptyView(
    isArchivedTab: Boolean,
    isSearching: Boolean,
    onAddHabit: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 40.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, HabitTheme.customColors.subtleBorder, RoundedCornerShape(24.dp))
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = when {
                    isSearching -> "No habits match your search"
                    isArchivedTab -> "No archived habits"
                    else -> "No habits added yet"
                },
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = when {
                    isSearching -> "Try searching for a different habit name."
                    isArchivedTab -> "Habits you archive will appear here for reference."
                    else -> "Start building consistency by creating your first daily habit!"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = HabitTheme.customColors.textSecondary
            )

            if (!isArchivedTab && !isSearching) {
                Spacer(modifier = Modifier.height(18.dp))
                Button(
                    onClick = onAddHabit,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add First Habit")
                }
            }
        }
    }
}
