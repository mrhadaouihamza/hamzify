package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.example.data.model.PredefinedIcons
import com.example.data.model.getIconVector
import com.example.ui.theme.HabitColorPalette
import com.example.ui.theme.HabitTheme
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditHabitSheet(
    habitToEdit: HabitEntity? = null,
    onDismiss: () -> Unit,
    onSave: (HabitEntity) -> Unit,
    onDelete: ((Long) -> Unit)? = null,
    onArchiveToggle: ((Long, Boolean) -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember { mutableStateOf(habitToEdit?.name ?: "") }
    var description by remember { mutableStateOf(habitToEdit?.description ?: "") }
    var selectedColor by remember { mutableStateOf(habitToEdit?.colorHex ?: HabitColorPalette.first()) }
    var selectedIcon by remember { mutableStateOf(habitToEdit?.iconName ?: "water_drop") }
    var frequencyType by remember { mutableStateOf(habitToEdit?.frequencyType ?: "DAILY") }
    var reminderTime by remember { mutableStateOf(habitToEdit?.reminderTime ?: "08:00") }

    val initialDays = remember {
        val daysList = habitToEdit?.targetDaysOfWeek?.split(",")?.mapNotNull { it.trim().toIntOrNull() } ?: listOf(1, 2, 3, 4, 5, 6, 7)
        mutableStateListOf<Int>().apply { addAll(daysList) }
    }

    val dayLabels = listOf(
        1 to "M",
        2 to "T",
        3 to "W",
        4 to "T",
        5 to "F",
        6 to "S",
        7 to "S"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null,
        modifier = Modifier.testTag("add_edit_habit_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 20.dp, bottom = 24.dp)
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (habitToEdit == null) "New Habit" else "Edit Habit",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

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
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Habit Name Input
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Habit Name") },
                placeholder = { Text("e.g. Read 20 pages, Morning Jog") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = HabitTheme.customColors.subtleBorder
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("habit_name_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Description Input
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (Optional)") },
                placeholder = { Text("Why is this habit important to you?") },
                maxLines = 2,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = HabitTheme.customColors.subtleBorder
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Color & Icon Preview
            Text(
                text = "Choose Icon & Color",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Color Palette Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HabitColorPalette.forEach { hex ->
                    val color = Color(android.graphics.Color.parseColor(hex))
                    val isSelected = selectedColor == hex
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(color)
                            .clickable { selectedColor = hex }
                            .then(
                                if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Icons Horizontal Selector
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(PredefinedIcons) { (iconKey, _) ->
                    val isSelected = selectedIcon == iconKey
                    val color = Color(android.graphics.Color.parseColor(selectedColor))
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) color else HabitTheme.customColors.surfaceSubtle)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) color else HabitTheme.customColors.subtleBorder,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { selectedIcon = iconKey },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getIconVector(iconKey),
                            contentDescription = iconKey,
                            tint = if (isSelected) Color.White else color,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Frequency Settings
            Text(
                text = "Frequency",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "DAILY" to "Daily",
                    "WEEKDAYS" to "Weekdays",
                    "WEEKENDS" to "Weekends",
                    "CUSTOM" to "Custom"
                ).forEach { (freqKey, label) ->
                    val isSelected = frequencyType == freqKey
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else HabitTheme.customColors.surfaceSubtle
                            )
                            .clickable { frequencyType = freqKey },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Custom days selection
            if (frequencyType == "CUSTOM") {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    dayLabels.forEach { (dayInt, label) ->
                        val isDaySelected = initialDays.contains(dayInt)
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isDaySelected) MaterialTheme.colorScheme.primary
                                    else HabitTheme.customColors.surfaceSubtle
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isDaySelected) MaterialTheme.colorScheme.primary else HabitTheme.customColors.subtleBorder,
                                    shape = CircleShape
                                )
                                .clickable {
                                    if (isDaySelected) {
                                        if (initialDays.size > 1) initialDays.remove(dayInt)
                                    } else {
                                        initialDays.add(dayInt)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (isDaySelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // Save Button
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val habit = HabitEntity(
                            id = habitToEdit?.id ?: 0,
                            name = name.trim(),
                            description = description.trim(),
                            category = habitToEdit?.category ?: "General",
                            iconName = selectedIcon,
                            colorHex = selectedColor,
                            frequencyType = frequencyType,
                            targetDaysOfWeek = initialDays.sorted().joinToString(","),
                            targetCountPerDay = 1,
                            timeOfDay = "ANYTIME",
                            reminderTime = reminderTime,
                            startDateEpochDay = habitToEdit?.startDateEpochDay ?: LocalDate.now().toEpochDay(),
                            isArchived = habitToEdit?.isArchived ?: false
                        )
                        onSave(habit)
                    }
                },
                enabled = name.isNotBlank(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_habit_button")
            ) {
                Text(
                    text = if (habitToEdit == null) "Create Habit" else "Save Changes",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            // Edit actions: Archive / Delete
            if (habitToEdit != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (onArchiveToggle != null) {
                        TextButton(
                            onClick = {
                                onArchiveToggle(habitToEdit.id, !habitToEdit.isArchived)
                                onDismiss()
                            }
                        ) {
                            Text(
                                text = if (habitToEdit.isArchived) "Unarchive Habit" else "Archive Habit",
                                color = HabitTheme.customColors.textSecondary
                            )
                        }
                    }

                    if (onDelete != null) {
                        TextButton(
                            onClick = {
                                onDelete(habitToEdit.id)
                                onDismiss()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Delete",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}
