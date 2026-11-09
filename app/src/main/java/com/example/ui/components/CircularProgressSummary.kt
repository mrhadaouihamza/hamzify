package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ElectricBolt
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.HabitTheme

@Composable
fun CircularProgressSummary(
    completedCount: Int,
    totalCount: Int,
    streakCount: Int,
    modifier: Modifier = Modifier
) {
    val progress = if (totalCount > 0) {
        (completedCount.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "progress_anim"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val successColor = HabitTheme.customColors.success
    val surfaceColor = MaterialTheme.colorScheme.surface
    val borderColor = HabitTheme.customColors.subtleBorder
    val secondaryText = HabitTheme.customColors.textSecondary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(surfaceColor)
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(24.dp))
            .padding(20.dp)
            .testTag("circular_progress_summary")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Circular Ring with Percentage
            Box(
                modifier = Modifier.size(118.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(110.dp)) {
                    val strokeWidth = 10.dp.toPx()
                    val arcSize = size.width - strokeWidth
                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                    // Track background
                    drawArc(
                        color = primaryColor.copy(alpha = 0.12f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(arcSize, arcSize),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Animated Progress arc with gradient
                    if (animatedProgress > 0f) {
                        val brush = Brush.sweepGradient(
                            listOf(
                                primaryColor,
                                successColor,
                                primaryColor
                            )
                        )
                        drawArc(
                            brush = brush,
                            startAngle = -90f,
                            sweepAngle = 360f * animatedProgress,
                            useCenter = false,
                            topLeft = topLeft,
                            size = Size(arcSize, arcSize),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val percentInt = (animatedProgress * 100).toInt()
                    Text(
                        text = "$percentInt%",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$completedCount of $totalCount",
                        style = MaterialTheme.typography.labelSmall,
                        color = secondaryText
                    )
                }
            }

            Spacer(modifier = Modifier.width(18.dp))

            // Details and Motivator
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                val motivationalText = when {
                    totalCount == 0 -> "No habits planned today"
                    completedCount == totalCount -> "All done for today! 🎉"
                    completedCount > 0 && completedCount >= totalCount / 2 -> "Great momentum, keep going!"
                    completedCount > 0 -> "Off to a productive start ✨"
                    else -> "Ready to seize the day?"
                }

                Text(
                    text = "Daily Progress",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = primaryColor
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = motivationalText,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 22.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Streak pill badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(primaryColor.copy(alpha = 0.08f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocalFireDepartment,
                        contentDescription = "Streak",
                        tint = Color(0xFFFF9F1C),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (streakCount > 0) "$streakCount days active streak" else "Start a new streak!",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
