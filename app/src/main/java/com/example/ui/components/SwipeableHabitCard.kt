package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.HabitEntity
import kotlin.math.roundToInt

// Habit360 inspired action colors
val HabitActionPink = Color(0xFFFF5D8F) // Skip pink button
val HabitActionTeal = Color(0xFF00B4D8) // Done teal button

@Composable
fun SwipeableHabitCard(
    habit: HabitEntity,
    isCompleted: Boolean,
    streak: Int,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    onDone: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
    isSkipped: Boolean = false,
    isOverdue: Boolean = false
) {
    val density = LocalDensity.current
    // Total width of reveal actions: 2 buttons of ~70dp + spacing = ~148dp
    val actionRevealWidthDp = 148.dp
    val actionRevealWidthPx = with(density) { actionRevealWidthDp.toPx() }

    var swipeOffset by remember { mutableFloatStateOf(0f) }

    val animatedOffset by animateFloatAsState(
        targetValue = swipeOffset,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "swipe_card_offset"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(82.dp)
            .clip(RoundedCornerShape(20.dp))
            .testTag("swipeable_habit_card_${habit.id}")
    ) {
        // Background Actions (Revealed when swiped left)
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(actionRevealWidthDp)
                .fillMaxHeight()
                .padding(vertical = 4.dp, horizontal = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pink "Skip" button (Swiping habit card to the left and selecting Skip)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(16.dp))
                    .background(HabitActionPink)
                    .clickable {
                        swipeOffset = 0f
                        onSkip()
                    }
                    .testTag("habit_action_skip_${habit.id}"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.FastForward,
                        contentDescription = "Skip",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isSkipped) "Unskip" else "Skip",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = Color.White
                    )
                }
            }

            // Teal/Cyan "Done" button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(16.dp))
                    .background(HabitActionTeal)
                    .clickable {
                        swipeOffset = 0f
                        onDone()
                    }
                    .testTag("habit_action_done_${habit.id}"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = "Done",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Done",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = Color.White
                    )
                }
            }
        }

        // Foreground Card (Slides horizontally)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(x = animatedOffset.roundToInt(), y = 0) }
                .pointerInput(habit.id) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            // If dragged left past 35% of reveal width, snap to opened
                            swipeOffset = if (swipeOffset < -actionRevealWidthPx * 0.35f) {
                                -actionRevealWidthPx
                            } else {
                                0f
                            }
                        },
                        onDragCancel = {
                            swipeOffset = 0f
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            val newOffset = (swipeOffset + dragAmount).coerceIn(-actionRevealWidthPx, 0f)
                            swipeOffset = newOffset
                        }
                    )
                }
        ) {
            HabitCard(
                habit = habit,
                isCompleted = isCompleted,
                streak = streak,
                isSkipped = isSkipped,
                isOverdue = isOverdue,
                onToggle = onToggle,
                onClick = {
                    if (swipeOffset < -10f) {
                        // If card was swiped open, tapping it snaps it closed
                        swipeOffset = 0f
                    } else {
                        // Open habit detail modal
                        onClick()
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
