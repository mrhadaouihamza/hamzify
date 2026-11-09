package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.FormatListBulleted
import androidx.compose.material.icons.rounded.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Today : Screen(
        route = "today",
        title = "Today",
        selectedIcon = Icons.Rounded.CheckCircle,
        unselectedIcon = Icons.Outlined.CheckCircle
    )

    object Habits : Screen(
        route = "habits",
        title = "Habits",
        selectedIcon = Icons.Rounded.FormatListBulleted,
        unselectedIcon = Icons.Outlined.FormatListBulleted
    )

    object Insights : Screen(
        route = "insights",
        title = "Insights",
        selectedIcon = Icons.Rounded.AutoGraph,
        unselectedIcon = Icons.Outlined.AutoGraph
    )

    object Profile : Screen(
        route = "profile",
        title = "Profile",
        selectedIcon = Icons.Rounded.Person,
        unselectedIcon = Icons.Outlined.Person
    )
}

val BottomNavItems = listOf(
    Screen.Today,
    Screen.Insights,
    Screen.Profile
)
