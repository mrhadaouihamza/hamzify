package com.example.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.HabitRepository
import com.example.ui.navigation.BottomNavItems
import com.example.ui.navigation.Screen
import com.example.ui.screens.habits.HabitsScreen
import com.example.ui.screens.habits.HabitsViewModel
import com.example.ui.screens.insights.InsightsScreen
import com.example.ui.screens.insights.InsightsViewModel
import com.example.ui.screens.profile.AppThemeMode
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.profile.ProfileViewModel
import com.example.ui.screens.today.TodayScreen
import com.example.ui.screens.today.TodayViewModel
import com.example.ui.theme.HabitFlowTheme
import com.example.ui.theme.HabitTheme

@Composable
fun MainAppScreen(
    repository: HabitRepository
) {
    val todayViewModel = remember { TodayViewModel(repository) }
    val habitsViewModel = remember { HabitsViewModel(repository) }
    val insightsViewModel = remember { InsightsViewModel(repository) }
    val profileViewModel = remember { ProfileViewModel(repository) }

    val themeMode by profileViewModel.themeMode.collectAsState()
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
        AppThemeMode.SYSTEM -> isSystemDark
    }

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Today) }

    HabitFlowTheme(darkTheme = isDark) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                            width = 1.dp,
                            color = HabitTheme.customColors.subtleBorder
                        )
                ) {
                    NavigationBar(
                        containerColor = Color.Transparent,
                        tonalElevation = 0.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .height(64.dp)
                            .testTag("main_bottom_nav_bar")
                    ) {
                        BottomNavItems.forEach { screen ->
                            val isSelected = currentScreen.route == screen.route
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { currentScreen = screen },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                        contentDescription = screen.title
                                    )
                                },
                                label = {
                                    Text(
                                        text = screen.title,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 11.sp
                                        )
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = HabitTheme.customColors.textSecondary,
                                    unselectedTextColor = HabitTheme.customColors.textSecondary,
                                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                ),
                                modifier = Modifier.testTag("nav_item_${screen.route}")
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = paddingValues.calculateBottomPadding())
            ) {
                Crossfade(targetState = currentScreen, label = "screen_crossfade") { screen ->
                    when (screen) {
                        is Screen.Today -> TodayScreen(
                            viewModel = todayViewModel,
                            onNavigateToProfile = { currentScreen = Screen.Profile }
                        )
                        is Screen.Habits -> HabitsScreen(
                            viewModel = habitsViewModel
                        )
                        is Screen.Insights -> InsightsScreen(
                            viewModel = insightsViewModel
                        )
                        is Screen.Profile -> ProfileScreen(
                            viewModel = profileViewModel
                        )
                    }
                }
            }
        }
    }
}
