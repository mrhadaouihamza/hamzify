package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.getIconVector

@Composable
fun HabitIconBadge(
    iconName: String,
    colorHex: String,
    modifier: Modifier = Modifier,
    size: Dp = 46.dp,
    iconSize: Dp = 24.dp,
    shapeRadius: Dp = 14.dp
) {
    val habitColor = try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        Color(0xFF6C63FF)
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(shapeRadius))
            .background(habitColor.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = getIconVector(iconName),
            contentDescription = null,
            tint = habitColor,
            modifier = Modifier.size(iconSize)
        )
    }
}
