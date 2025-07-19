package com.yovinchen.bookkeeping.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yovinchen.bookkeeping.model.BookkeepingRecord
import com.yovinchen.bookkeeping.model.TransactionType
import java.text.NumberFormat
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * 详细分析报表组件
 * 显示按分类统计的详细信息
 */
@Composable
fun DetailedAnalysisReport(
    records: List<BookkeepingRecord>,
    startMonth: YearMonth,
    endMonth: YearMonth,
    modifier: Modifier = Modifier
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.CHINA)
    
    // 按类型分组
    val incomeRecords = records.filter { it.type == TransactionType.INCOME }
    val expenseRecords = records.filter { it.type == TransactionType.EXPENSE }
    
    // 按分类统计
    val incomeByCategory = incomeRecords.groupBy { it.category }
        .mapValues { it.value.sumOf { record -> record.amount } }
        .toList()
        .sortedByDescending { it.second }
    
    val expenseByCategory = expenseRecords.groupBy { it.category }
        .mapValues { it.value.sumOf { record -> record.amount } }
        .toList()
        .sortedByDescending { it.second }
    
    // 总收入和总支出
    val totalIncome = incomeRecords.sumOf { it.amount }
    val totalExpense = expenseRecords.sumOf { it.amount }
    
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 时间范围标题
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text = if (startMonth == endMonth) {
                        "统计期间：${startMonth.format(DateTimeFormatter.ofPattern("yyyy年MM月"))}"
                    } else {
                        "统计期间：${startMonth.format(DateTimeFormatter.ofPattern("yyyy年MM月"))} 至 ${endMonth.format(DateTimeFormatter.ofPattern("yyyy年MM月"))}"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
        
        // 支出分类详情
        if (expenseByCategory.isNotEmpty()) {
            item {
                CategoryDetailCard(
                    title = "支出分类明细",
                    categoryData = expenseByCategory,
                    total = totalExpense,
                    color = MaterialTheme.colorScheme.error,
                    currencyFormatter = currencyFormatter
                )
            }
        }
        
        // 收入分类详情
        if (incomeByCategory.isNotEmpty()) {
            item {
                CategoryDetailCard(
                    title = "收入分类明细",
                    categoryData = incomeByCategory,
                    total = totalIncome,
                    color = MaterialTheme.colorScheme.primary,
                    currencyFormatter = currencyFormatter
                )
            }
        }
        
        // 分类占比前5名
        if (expenseByCategory.isNotEmpty()) {
            item {
                TopCategoriesCard(
                    title = "支出TOP5",
                    categoryData = expenseByCategory.take(5),
                    total = totalExpense,
                    color = MaterialTheme.colorScheme.error,
                    currencyFormatter = currencyFormatter
                )
            }
        }
    }
}

/**
 * 分类详情卡片
 */
@Composable
private fun CategoryDetailCard(
    title: String,
    categoryData: List<Pair<String, Double>>,
    total: Double,
    color: Color,
    currencyFormatter: NumberFormat
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // 总计
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "总计",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = currencyFormatter.format(total),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            // 分类列表
            categoryData.forEach { (category, amount) ->
                val percentage = if (total > 0) (amount / total * 100) else 0.0
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${String.format("%.1f", percentage)}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Text(
                        text = currencyFormatter.format(amount),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // 进度条
                LinearProgressIndicator(
                    progress = (percentage / 100).toFloat(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = color.copy(alpha = 0.8f),
                    trackColor = color.copy(alpha = 0.2f)
                )
            }
        }
    }
}

/**
 * TOP分类卡片
 */
@Composable
private fun TopCategoriesCard(
    title: String,
    categoryData: List<Pair<String, Double>>,
    total: Double,
    color: Color,
    currencyFormatter: NumberFormat
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            categoryData.forEachIndexed { index, (category, amount) ->
                val percentage = if (total > 0) (amount / total * 100) else 0.0
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 排名
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    when (index) {
                                        0 -> Color(0xFFFFD700) // 金色
                                        1 -> Color(0xFFC0C0C0) // 银色
                                        2 -> Color(0xFFCD7F32) // 铜色
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (index < 3) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Text(
                            text = category,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                    
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = currencyFormatter.format(amount),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${String.format("%.1f", percentage)}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}