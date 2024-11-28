package com.yovinchen.bookkeeping.ui.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yovinchen.bookkeeping.ui.components.CategoryPieChart
import com.yovinchen.bookkeeping.ui.components.MonthYearPicker
import com.yovinchen.bookkeeping.viewmodel.AnalysisType
import com.yovinchen.bookkeeping.viewmodel.AnalysisViewModel
import com.yovinchen.bookkeeping.viewmodel.CategoryStat

@Composable
fun AnalysisScreen(
    modifier: Modifier = Modifier,
    viewModel: AnalysisViewModel = viewModel()
) {
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val selectedType by viewModel.selectedAnalysisType.collectAsState()
    val categoryStats by viewModel.categoryStats.collectAsState()
    var showMonthPicker by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 月份选择器
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    viewModel.setSelectedMonth(selectedMonth.minusMonths(1))
                }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "上个月")
                }

                Text(
                    text = "${selectedMonth.year}年${selectedMonth.monthValue}月",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.clickable { showMonthPicker = true }
                )

                IconButton(onClick = {
                    viewModel.setSelectedMonth(selectedMonth.plusMonths(1))
                }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "下个月")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 分析类型选择
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AnalysisType.entries.forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { viewModel.setAnalysisType(type) },
                        label = {
                            Text(
                                when (type) {
                                    AnalysisType.EXPENSE -> "支出分析"
                                    AnalysisType.INCOME -> "收入分析"
                                    AnalysisType.TREND -> "收支趋势"
                                }
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 统计内容
            when (selectedType) {
                AnalysisType.EXPENSE, AnalysisType.INCOME -> {
                    Text(
                        text = if (selectedType == AnalysisType.EXPENSE) "" else "",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    if (categoryStats.isNotEmpty()) {
                        val pieChartData = categoryStats.map { stat ->
                            stat.category to stat.percentage.toFloat()
                        }
                        CategoryPieChart(
                            categoryData = pieChartData,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "分类明细",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        Text(
                            text = "暂无数据",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp)
                        )
                    }
                }
                AnalysisType.TREND -> {
                    Text(
                        text = "收支趋势分析（开发中）",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }

        // 分类统计列表
        if (selectedType != AnalysisType.TREND && categoryStats.isNotEmpty()) {
            items(categoryStats) { stat ->
                CategoryStatItem(stat)
            }
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

@SuppressLint("DefaultLocale")
@Composable
fun CategoryStatItem(stat: CategoryStat) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stat.category,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = String.format("%.2f", stat.amount),
                style = MaterialTheme.typography.bodyLarge
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LinearProgressIndicator(
                progress = { stat.percentage.toFloat() / 100f },
                modifier = Modifier
                    .weight(1f)
                    .height(8.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(4.dp)
                    ),
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = String.format("%.1f%%", stat.percentage),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
