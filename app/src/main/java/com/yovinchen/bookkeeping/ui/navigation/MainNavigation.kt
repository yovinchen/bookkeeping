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
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Surface
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf

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
    object Budget : Screen(
        "budget",
        "预算管理"
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
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
                                androidx.compose.animation.AnimatedContent(
                                    targetState = selected,
                                    transitionSpec = {
                                        scaleIn(
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessMedium
                                            )
                                        ) + fadeIn() togetherWith
                                        scaleOut(
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessMedium
                                            )
                                        ) + fadeOut()
                                    }
                                ) { isSelected ->
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = screen.title,
                                        modifier = Modifier
                                            .size(if (isSelected) 28.dp else 24.dp)
                                            .animateContentSize(
                                                animationSpec = spring(
                                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                                    stiffness = Spring.StiffnessMedium
                                                )
                                            ),
                                        tint = Color.Unspecified
                                    )
                                }
                            }
                        },
                        label = { 
                            AnimatedContent(
                                targetState = selected,
                                transitionSpec = {
                                    (fadeIn(animationSpec = tween(300)) +
                                     expandVertically(animationSpec = tween(300))) togetherWith
                                    (fadeOut(animationSpec = tween(200)) +
                                     shrinkVertically(animationSpec = tween(200)))
                                }
                            ) { isSelected ->
                                Text(
                                    text = screen.title,
                                    style = if (isSelected) 
                                        MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                    else 
                                        MaterialTheme.typography.labelMedium
                                )
                            }
                        },
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
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(200))
            },
            popEnterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(200))
            }
        ) {
            composable(
                Screen.Home.route,
                enterTransition = {
                    when (initialState.destination.route) {
                        Screen.Analysis.route -> slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(400, easing = FastOutSlowInEasing)
                        ) + fadeIn(animationSpec = tween(400))
                        Screen.Settings.route -> slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(400, easing = FastOutSlowInEasing)
                        ) + fadeIn(animationSpec = tween(400))
                        else -> fadeIn(animationSpec = tween(300))
                    }
                },
                exitTransition = {
                    when (targetState.destination.route) {
                        Screen.Analysis.route -> slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(400, easing = FastOutSlowInEasing)
                        ) + fadeOut(animationSpec = tween(200))
                        Screen.Settings.route -> slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(400, easing = FastOutSlowInEasing)
                        ) + fadeOut(animationSpec = tween(200))
                        else -> fadeOut(animationSpec = tween(300))
                    }
                }
            ) { 
                HomeScreen()
            }
            
            composable(
                Screen.Analysis.route,
                enterTransition = {
                    when (initialState.destination.route) {
                        Screen.Home.route -> slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(400, easing = FastOutSlowInEasing)
                        ) + fadeIn(animationSpec = tween(400))
                        Screen.Settings.route -> slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(400, easing = FastOutSlowInEasing)
                        ) + fadeIn(animationSpec = tween(400))
                        else -> fadeIn(animationSpec = tween(300))
                    }
                },
                exitTransition = {
                    when (targetState.destination.route) {
                        Screen.Home.route -> slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(400, easing = FastOutSlowInEasing)
                        ) + fadeOut(animationSpec = tween(200))
                        Screen.Settings.route -> slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(400, easing = FastOutSlowInEasing)
                        ) + fadeOut(animationSpec = tween(200))
                        else -> fadeOut(animationSpec = tween(300))
                    }
                }
            ) {
                AnalysisScreen(
                    onNavigateToCategoryDetail = { category, startMonth, endMonth ->
                        navController.navigate(Screen.CategoryDetail.createRoute(category, startMonth, endMonth))
                    },
                    onNavigateToMemberDetail = { memberName, startMonth, endMonth, analysisType ->
                        navController.navigate(Screen.MemberDetail.createRoute(memberName, "", startMonth, endMonth, analysisType))
                    }
                )
            }
            
            composable(
                Screen.Settings.route,
                enterTransition = {
                    when (initialState.destination.route) {
                        Screen.Home.route -> slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(400, easing = FastOutSlowInEasing)
                        ) + fadeIn(animationSpec = tween(400))
                        Screen.Analysis.route -> slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(400, easing = FastOutSlowInEasing)
                        ) + fadeIn(animationSpec = tween(400))
                        else -> fadeIn(animationSpec = tween(300))
                    }
                },
                exitTransition = {
                    when (targetState.destination.route) {
                        Screen.Home.route -> slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(400, easing = FastOutSlowInEasing)
                        ) + fadeOut(animationSpec = tween(200))
                        Screen.Analysis.route -> slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(400, easing = FastOutSlowInEasing)
                        ) + fadeOut(animationSpec = tween(200))
                        else -> fadeOut(animationSpec = tween(300))
                    }
                }
            ) {
                SettingsScreen(
                    currentTheme = currentTheme,
                    onThemeChange = onThemeChange,
                    onNavigateToBudget = {
                        navController.navigate(Screen.Budget.route)
                    }
                )
            }
            
            composable(Screen.Budget.route) {
                BudgetScreen()
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
