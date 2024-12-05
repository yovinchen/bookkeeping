package com.yovinchen.bookkeeping.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yovinchen.bookkeeping.model.AnalysisType
import com.yovinchen.bookkeeping.ui.components.CategoryPieChart
import com.yovinchen.bookkeeping.ui.components.CategoryStatItem
import com.yovinchen.bookkeeping.ui.components.MonthYearPicker
import com.yovinchen.bookkeeping.viewmodel.AnalysisViewModel
import java.time.YearMonth
import java.time.format.DateTimeFormatter

enum class ViewMode {
    CATEGORY, MEMBER
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(
    onNavigateToCategoryDetail: (String, YearMonth) -> Unit,
    onNavigateToMemberDetail: (String, YearMonth, AnalysisType) -> Unit
) {
    val viewModel: AnalysisViewModel = viewModel()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val selectedAnalysisType by viewModel.selectedAnalysisType.collectAsState()
    val categoryStats by viewModel.categoryStats.collectAsState()
    val memberStats by viewModel.memberStats.collectAsState()

    var showMonthPicker by remember { mutableStateOf(false) }
    var showViewModeMenu by remember { mutableStateOf(false) }
    var currentViewMode by rememberSaveable { mutableStateOf(ViewMode.CATEGORY) }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 时间选择按钮行
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = { showMonthPicker = true }) {
                    Text(selectedMonth.format(DateTimeFormatter.ofPattern("yyyy年MM月")))
                }
            }

            // 分析类型和视图模式选择行
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 分类/成员切换下拉菜单
                Box {
                    Button(
                        onClick = { showViewModeMenu = true }
                    ) {
                        Text(if (currentViewMode == ViewMode.CATEGORY) "分类" else "成员")
                        Icon(Icons.Default.ArrowDropDown, "切换视图")
                    }
                    DropdownMenu(
                        expanded = showViewModeMenu,
                        onDismissRequest = { showViewModeMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("分类") },
                            onClick = {
                                currentViewMode = ViewMode.CATEGORY
                                showViewModeMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("成员") },
                            onClick = {
                                currentViewMode = ViewMode.MEMBER
                                showViewModeMenu = false
                            }
                        )
                    }
                }

                // 类型切换
                Row {
                    AnalysisType.values().forEach { type ->
                        FilterChip(
                            selected = selectedAnalysisType == type,
                            onClick = { viewModel.setAnalysisType(type) },
                            label = {
                                Text(
                                    when (type) {
                                        AnalysisType.EXPENSE -> "支出"
                                        AnalysisType.INCOME -> "收入"
                                        AnalysisType.TREND -> "趋势"
                                    }
                                )
                            },
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }

            // 使用LazyColumn包含饼图和列表
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                // 添加饼图作为第一个项目
                if (selectedAnalysisType != AnalysisType.TREND) {
                    item {
                        CategoryPieChart(
                            categoryData = categoryStats.map { Pair(it.category, it.percentage.toFloat()) },
                            memberData = memberStats.map { Pair(it.category, it.percentage.toFloat()) },
                            currentViewMode = currentViewMode == ViewMode.MEMBER,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(bottom = 16.dp),
                            onCategoryClick = { category ->
                                if (currentViewMode == ViewMode.CATEGORY) {
                                    onNavigateToCategoryDetail(category, selectedMonth)
                                } else {
                                    onNavigateToMemberDetail(category, selectedMonth, selectedAnalysisType)
                                }
                            }
                        )
                    }
                }

                // 添加统计列表项目
                items(if (currentViewMode == ViewMode.CATEGORY) categoryStats else memberStats) { stat ->
                    CategoryStatItem(
                        stat = stat,
                        onClick = {
                            if (currentViewMode == ViewMode.CATEGORY) {
                                onNavigateToCategoryDetail(stat.category, selectedMonth)
                            } else {
                                onNavigateToMemberDetail(stat.category, selectedMonth, selectedAnalysisType)
                            }
                        }
                    )
                }
            }

            if (showMonthPicker) {
                MonthYearPicker(
                    selectedMonth = selectedMonth,
                    onMonthSelected = { month ->
                        viewModel.setSelectedMonth(month)
                        showMonthPicker = false
                    },
                    onDismiss = { showMonthPicker = false }
                )
            }
        }
    }
}
