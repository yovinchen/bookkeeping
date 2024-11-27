package com.yovinchen.bookkeeping.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.yovinchen.bookkeeping.model.ThemeMode
import com.yovinchen.bookkeeping.ui.screen.HomeScreen
import com.yovinchen.bookkeeping.ui.screen.SettingsScreen

sealed class Screen(val route: String, val icon: @Composable () -> Unit, val label: String) {
    object Home : Screen(
        route = "home",
        icon = { Icon(Icons.Default.Home, contentDescription = "主页") },
        label = "主页"
    )
    object Settings : Screen(
        route = "settings",
        icon = { Icon(Icons.Default.Settings, contentDescription = "设置") },
        label = "设置"
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigation(
    currentTheme: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit
) {
    val navController = rememberNavController()
    val items = listOf(Screen.Home, Screen.Settings)

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { screen ->
                    NavigationBarItem(
                        icon = screen.icon,
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Home.route) {
                HomeScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    currentTheme = currentTheme,
                    onThemeChange = onThemeChange
                )
            }
        }
    }
}
