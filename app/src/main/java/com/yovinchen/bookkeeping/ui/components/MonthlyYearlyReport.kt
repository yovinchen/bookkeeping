package com.yovinchen.bookkeeping.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * 月度/年度报表组件
 * 显示收支对比、盈余情况等统计信息
 */
@Composable
fun MonthlyYearlyReport(
    records: List<BookkeepingRecord>,
    period: String, // "月度" 或 "年度"
    startMonth: YearMonth,
    endMonth: YearMonth,
    modifier: Modifier = Modifier
) {
    val totalIncome = records
        .filter { it.type == TransactionType.INCOME }
        .sumOf { it.amount }
    
    val totalExpense = records
        .filter { it.type == TransactionType.EXPENSE }
        .sumOf { it.amount }
    
    val balance = totalIncome - totalExpense
    val savingsRate = if (totalIncome > 0) {
        ((totalIncome - totalExpense) / totalIncome * 100).coerceAtLeast(0.0)
    } else 0.0
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 标题
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$period 报表",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = if (startMonth == endMonth) {
                        startMonth.format(DateTimeFormatter.ofPattern("yyyy年MM月"))
                    } else {
                        "${startMonth.format(DateTimeFormatter.ofPattern("yyyy年MM月"))} - ${endMonth.format(DateTimeFormatter.ofPattern("yyyy年MM月"))}"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            HorizontalDivider()
            
            // 收支对比
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // 收入
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "总收入",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(totalIncome),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // 分隔线
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(40.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
                
                // 支出
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "总支出",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(totalExpense),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            // 盈余情况
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (balance >= 0) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.errorContainer
                    }
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (balance >= 0) "盈余" else "亏损",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (balance >= 0) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onErrorContainer
                        }
                    )
                    Text(
                        text = formatCurrency(kotlin.math.abs(balance)),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (balance >= 0) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onErrorContainer
                        }
                    )
                }
            }
            
            // 储蓄率
            if (totalIncome > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "储蓄率",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Box(
                        modifier = Modifier.weight(2f)
                    ) {
                        LinearProgressIndicator(
                            progress = (savingsRate / 100).toFloat().coerceIn(0f, 1f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = when {
                                savingsRate >= 30 -> MaterialTheme.colorScheme.primary
                                savingsRate >= 10 -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.error
                            }
                        )
                    }
                    
                    Text(
                        text = "${String.format("%.1f", savingsRate)}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp),
                        color = when {
                            savingsRate >= 30 -> MaterialTheme.colorScheme.primary
                            savingsRate >= 10 -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.error
                        }
                    )
                }
            }
            
            // 日均消费
            val dayCount = if (period == "月度") {
                // 计算月度天数
                java.time.temporal.ChronoUnit.DAYS.between(
                    startMonth.atDay(1),
                    endMonth.atEndOfMonth()
                ) + 1
            } else {
                // 计算年度天数
                java.time.temporal.ChronoUnit.DAYS.between(
                    startMonth.atDay(1),
                    endMonth.atEndOfMonth()
                ) + 1
            }
            
            val dailyAverage = if (dayCount > 0) totalExpense / dayCount else 0.0
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "日均消费",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatCurrency(dailyAverage),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * 格式化货币
 */
private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale.CHINA)
    return format.format(amount)
}