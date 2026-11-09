package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.HabitTheme

@Composable
fun CompletionCheckbox(
    isCompleted: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
    activeColor: Color = HabitTheme.customColors.success
) {
    val scale by animateFloatAsState(
        targetValue = if (isCompleted) 1.05f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "checkbox_scale"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isCompleted) activeColor else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "checkbox_bg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isCompleted) activeColor else HabitTheme.customColors.subtleBorder.copy(alpha = 0.9f),
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "checkbox_border"
    )

    val checkmarkAlpha by animateFloatAsState(
        targetValue = if (isCompleted) 1f else 0f,
        label = "checkbox_icon_alpha"
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .clip(CircleShape)
            .border(width = 2.dp, color = borderColor, shape = CircleShape)
            .background(color = bgColor, shape = CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggle
            )
            .testTag("habit_completion_checkbox"),
        contentAlignment = Alignment.Center
    ) {
        if (checkmarkAlpha > 0.05f) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = if (isCompleted) "Completed" else "Mark complete",
                tint = Color.White.copy(alpha = checkmarkAlpha),
                modifier = Modifier
                    .size(size * 0.58f)
                    .scale(checkmarkAlpha)
            )
        }
    }
}
