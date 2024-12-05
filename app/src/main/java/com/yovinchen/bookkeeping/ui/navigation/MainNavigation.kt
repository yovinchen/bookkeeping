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
    object CategoryDetail : Screen(
        "category_detail/{category}/{startMonth}/{endMonth}",
        "分类详情"
    ) {
        fun createRoute(
            category: String,
            startMonth: YearMonth,
            endMonth: YearMonth
        ): String {
            return "category_detail/$category/${startMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"))}/${endMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"))}"
        }
    }
    object MemberDetail : Screen(
        "member_detail/{memberName}/{category}/{startMonth}/{endMonth}?type={type}",
        "成员详情"
    ) {
        fun createRoute(
            memberName: String,
            category: String,
            startMonth: YearMonth,
            endMonth: YearMonth,
            type: AnalysisType
        ): String {
            return "member_detail/$memberName/$category/${startMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"))}/${endMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"))}?type=${type.name}"
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
                    onNavigateToCategoryDetail = { category, startMonth, endMonth ->
                        navController.navigate(Screen.CategoryDetail.createRoute(category, startMonth, endMonth))
                    },
                    onNavigateToMemberDetail = { memberName, startMonth, endMonth, analysisType ->
                        navController.navigate(Screen.MemberDetail.createRoute(memberName, "", startMonth, endMonth, analysisType))
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
                    navArgument("startMonth") { type = NavType.StringType },
                    navArgument("endMonth") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val category = backStackEntry.arguments?.getString("category") ?: ""
                val startMonth = YearMonth.parse(
                    backStackEntry.arguments?.getString("startMonth") ?: "",
                    DateTimeFormatter.ofPattern("yyyy-MM")
                )
                val endMonth = YearMonth.parse(
                    backStackEntry.arguments?.getString("endMonth") ?: "",
                    DateTimeFormatter.ofPattern("yyyy-MM")
                )
                CategoryDetailScreen(
                    category = category,
                    startMonth = startMonth,
                    endMonth = endMonth,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToMemberDetail = { memberName ->
                        navController.navigate(
                            Screen.MemberDetail.createRoute(
                                memberName = memberName,
                                category = category,
                                startMonth = startMonth,
                                endMonth = endMonth,
                                type = AnalysisType.EXPENSE
                            )
                        )
                    }
                )
            }
            composable(
                route = Screen.MemberDetail.route,
                arguments = listOf(
                    navArgument("memberName") { type = NavType.StringType },
                    navArgument("category") { type = NavType.StringType },
                    navArgument("startMonth") { type = NavType.StringType },
                    navArgument("endMonth") { type = NavType.StringType },
                    navArgument("type") {
                        type = NavType.StringType
                        defaultValue = AnalysisType.EXPENSE.name
                    }
                )
            ) { backStackEntry ->
                val memberName = backStackEntry.arguments?.getString("memberName") ?: ""
                val category = backStackEntry.arguments?.getString("category") ?: ""
                val startMonth = YearMonth.parse(
                    backStackEntry.arguments?.getString("startMonth") ?: "",
                    DateTimeFormatter.ofPattern("yyyy-MM")
                )
                val endMonth = YearMonth.parse(
                    backStackEntry.arguments?.getString("endMonth") ?: "",
                    DateTimeFormatter.ofPattern("yyyy-MM")
                )
                val type = AnalysisType.valueOf(
                    backStackEntry.arguments?.getString("type") ?: AnalysisType.EXPENSE.name
                )
                MemberDetailScreen(
                    memberName = memberName,
                    category = category,
                    startMonth = startMonth,
                    endMonth = endMonth,
                    analysisType = type,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
