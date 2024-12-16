package com.yovinchen.bookkeeping.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.yovinchen.bookkeeping.R
import com.yovinchen.bookkeeping.model.AnalysisType
import com.yovinchen.bookkeeping.model.ThemeMode
import com.yovinchen.bookkeeping.ui.screen.*
import androidx.compose.material3.*
import androidx.compose.ui.unit.dp
import java.time.YearMonth
import java.time.format.DateTimeFormatter

sealed class Screen(
    val route: String,
    val title: String,
    val iconResId: Int? = null
) {
    @Composable
    fun icon(): ImageVector? = iconResId?.let { ImageVector.vectorResource(it) }

    object Home : Screen(
        "home", 
        "记账",
        iconResId = R.drawable.account
    )
    object Analysis : Screen(
        "analysis", 
        "分析",
        iconResId = R.drawable.piechart
    )
    object Settings : Screen(
        "settings", 
        "设置",
        iconResId = R.drawable.setting
    )
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
    val items = listOf(
        Screen.Home,
        Screen.Analysis,
        Screen.Settings
    )
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                
                items.forEach { screen ->
                    val selected = currentRoute == screen.route
                    NavigationBarItem(
                        icon = { 
                            screen.icon()?.let { icon ->
                                Icon(
                                    imageVector = icon,
                                    contentDescription = screen.title,
                                    modifier = Modifier.size(24.dp),
                                    tint = Color.Unspecified
                                )
                            }
                        },
                        label = { Text(screen.title) },
                        selected = selected,
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
