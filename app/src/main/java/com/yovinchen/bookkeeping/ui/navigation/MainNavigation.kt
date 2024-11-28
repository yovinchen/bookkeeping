package com.yovinchen.bookkeeping.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.yovinchen.bookkeeping.model.ThemeMode
import com.yovinchen.bookkeeping.ui.screen.AnalysisScreen
import com.yovinchen.bookkeeping.ui.screen.CategoryDetailScreen
import com.yovinchen.bookkeeping.ui.screen.HomeScreen
import com.yovinchen.bookkeeping.ui.screen.SettingsScreen
import java.time.YearMonth
import java.time.format.DateTimeFormatter

sealed class Screen(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    data object Home : Screen("home", Icons.Default.Home, "主页")
    data object Analysis : Screen("analysis", Icons.Outlined.Analytics, "分析")
    data object Settings : Screen("settings", Icons.Default.Settings, "设置")
    data object CategoryDetail : Screen(
        "category_detail/{category}/{yearMonth}",
        Icons.Default.List,
        "分类详情"
    ) {
        fun createRoute(category: String, yearMonth: String) =
            "category_detail/$category/$yearMonth"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigation(
    currentTheme: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                listOf(
                    Screen.Home,
                    Screen.Analysis,
                    Screen.Settings
                ).forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen() }
            composable(Screen.Analysis.route) {
                AnalysisScreen(
                    onNavigateToCategoryDetail = { category, month ->
                        val monthStr = month.format(DateTimeFormatter.ofPattern("yyyy-MM"))
                        navController.navigate(Screen.CategoryDetail.createRoute(category, monthStr))
                    }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    currentTheme = currentTheme,
                    onThemeChange = onThemeChange
                )
            }

            composable(
                route = Screen.CategoryDetail.route,
                arguments = listOf(
                    navArgument("category") { type = NavType.StringType },
                    navArgument("yearMonth") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val category = backStackEntry.arguments?.getString("category") ?: return@composable
                val yearMonth = YearMonth.parse(
                    backStackEntry.arguments?.getString("yearMonth") ?: return@composable
                )
                CategoryDetailScreen(
                    category = category,
                    month = yearMonth,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
