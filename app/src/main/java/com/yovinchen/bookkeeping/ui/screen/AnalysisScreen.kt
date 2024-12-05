package com.yovinchen.bookkeeping.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yovinchen.bookkeeping.model.AnalysisType
import com.yovinchen.bookkeeping.model.CategoryStat
import com.yovinchen.bookkeeping.model.MemberStat
import com.yovinchen.bookkeeping.ui.components.CategoryPieChart
import com.yovinchen.bookkeeping.ui.components.CategoryStatItem
import com.yovinchen.bookkeeping.ui.components.DateRangePicker
import com.yovinchen.bookkeeping.viewmodel.AnalysisViewModel
import java.time.YearMonth

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
    val startMonth by viewModel.startMonth.collectAsState()
    val endMonth by viewModel.endMonth.collectAsState()
    val selectedAnalysisType by viewModel.selectedAnalysisType.collectAsState()
    val categoryStats by viewModel.categoryStats.collectAsState()
    val memberStats by viewModel.memberStats.collectAsState()

    var showViewModeMenu by remember { mutableStateOf(false) }
    var currentViewMode by rememberSaveable { mutableStateOf(ViewMode.CATEGORY) }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 时间区间选择
            DateRangePicker(
                startMonth = startMonth,
                endMonth = endMonth,
                onStartMonthSelected = viewModel::setStartMonth,
                onEndMonthSelected = viewModel::setEndMonth
            )

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
                    AnalysisType.entries.forEach { type ->
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
                            memberData = memberStats.map { Pair(it.member, it.percentage.toFloat()) },
                            currentViewMode = currentViewMode == ViewMode.MEMBER,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(bottom = 16.dp),
                            onCategoryClick = { category ->
                                if (currentViewMode == ViewMode.CATEGORY) {
                                    onNavigateToCategoryDetail(category, startMonth)
                                } else {
                                    onNavigateToMemberDetail(category, startMonth, selectedAnalysisType)
                                }
                            }
                        )
                    }
                }

                // 添加统计列表项目
                items(if (currentViewMode == ViewMode.CATEGORY) categoryStats else memberStats) { stat ->
                    val category = if (stat is CategoryStat) stat.category else null
                    val member = if (stat is MemberStat) stat.member else null

                    CategoryStatItem(
                        stat = stat,
                        onClick = {
                            if (currentViewMode == ViewMode.CATEGORY && category != null) {
                                onNavigateToCategoryDetail(category, startMonth)
                            } else if (currentViewMode == ViewMode.MEMBER && member != null) {
                                onNavigateToMemberDetail(member, startMonth, selectedAnalysisType)
                            }
                        }
                    )
                }
            }
        }
    }
}
