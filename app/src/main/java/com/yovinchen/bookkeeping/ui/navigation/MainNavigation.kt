package com.yovinchen.bookkeeping.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Settings
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
import com.yovinchen.bookkeeping.model.AnalysisType
import com.yovinchen.bookkeeping.model.ThemeMode
import com.yovinchen.bookkeeping.ui.screen.*
import java.time.YearMonth
import java.time.format.DateTimeFormatter

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector? = null
) {
    object Home : Screen("home", "记账", Icons.AutoMirrored.Filled.List)
    object Analysis : Screen("analysis", "分析", Icons.Default.Analytics)
    object Settings : Screen("settings", "设置", Icons.Default.Settings)
    object CategoryDetail : Screen("category_detail/{category}/{yearMonth}", "分类详情") {
        fun createRoute(category: String, yearMonth: YearMonth): String {
            return "category_detail/$category/${yearMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"))}"
        }
    }
    object MemberDetail : Screen("member_detail/{memberName}/{category}/{yearMonth}?type={type}", "成员详情") {
        fun createRoute(memberName: String, category: String, yearMonth: YearMonth, type: AnalysisType): String {
            return "member_detail/$memberName/$category/${yearMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"))}?type=${type.name}"
        }
    }

    companion object {
        fun bottomNavigationItems() = listOf(Home, Analysis, Settings)
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

                Screen.bottomNavigationItems().forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon!!, contentDescription = screen.title) },
                        label = { Text(screen.title) },
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
                    onNavigateToCategoryDetail = { category, yearMonth ->
                        navController.navigate(Screen.CategoryDetail.createRoute(category, yearMonth))
                    },
                    onNavigateToMemberDetail = { memberName, yearMonth, analysisType ->
                        navController.navigate(Screen.MemberDetail.createRoute(memberName, "", yearMonth, analysisType))
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
                val yearMonthStr = backStackEntry.arguments?.getString("yearMonth") ?: return@composable
                val yearMonth = YearMonth.parse(yearMonthStr)

                CategoryDetailScreen(
                    category = category,
                    yearMonth = yearMonth,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToMemberDetail = { memberName ->
                        navController.navigate(Screen.MemberDetail.createRoute(memberName, category, yearMonth, AnalysisType.EXPENSE))
                    }
                )
            }

            composable(
                route = Screen.MemberDetail.route,
                arguments = listOf(
                    navArgument("memberName") { type = NavType.StringType },
                    navArgument("category") { type = NavType.StringType },
                    navArgument("yearMonth") { type = NavType.StringType },
                    navArgument("type") { 
                        type = NavType.StringType
                        defaultValue = AnalysisType.EXPENSE.name
                    }
                )
            ) { backStackEntry ->
                val memberName = backStackEntry.arguments?.getString("memberName") ?: return@composable
                val category = backStackEntry.arguments?.getString("category") ?: return@composable
                val yearMonthStr = backStackEntry.arguments?.getString("yearMonth") ?: return@composable
                val yearMonth = YearMonth.parse(yearMonthStr)
                val type = backStackEntry.arguments?.getString("type")?.let {
                    try {
                        AnalysisType.valueOf(it)
                    } catch (e: IllegalArgumentException) {
                        AnalysisType.EXPENSE
                    }
                } ?: AnalysisType.EXPENSE

                MemberDetailScreen(
                    memberName = memberName,
                    yearMonth = yearMonth,
                    category = category,
                    analysisType = type,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
