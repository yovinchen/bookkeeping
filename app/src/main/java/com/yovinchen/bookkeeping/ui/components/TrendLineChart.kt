package com.yovinchen.bookkeeping.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.yovinchen.bookkeeping.model.BookkeepingRecord
import com.yovinchen.bookkeeping.model.TransactionType
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TrendLineChart(
    records: List<BookkeepingRecord>,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = isSystemInDarkTheme()
    var textColor = if (isDarkTheme) {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.87f).toArgb()
    } else {
        MaterialTheme.colorScheme.onSurface.toArgb()
    }
    
    var gridColor = if (isDarkTheme) {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f).toArgb()
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f).toArgb()
    }

    val incomeColor = MaterialTheme.colorScheme.primary.toArgb()
    val expenseColor = MaterialTheme.colorScheme.error.toArgb()

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp),
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                
                // 基本设置
                setDrawGridBackground(false)
                setDrawBorders(false)
                
                // X轴设置
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    this.textColor = textColor
                    this.gridColor = gridColor
                    setDrawGridLines(true)
                    setDrawAxisLine(true)
                    labelRotationAngle = -45f
                    textSize = 12f
                    yOffset = 10f
                }

                // Y轴设置
                axisLeft.apply {
                    this.textColor = textColor
                    this.gridColor = gridColor
                    setDrawGridLines(true)
                    setDrawAxisLine(true)
                    textSize = 12f
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return String.format("%.0f", value)
                        }
                    }
                }
                axisRight.isEnabled = false

                // 图例设置
                legend.apply {
                    this.textColor = textColor
                    this.textSize = 12f
                    isEnabled = true
                    yOffset = 10f
                }

                // 交互设置
                setTouchEnabled(true)
                isDragEnabled = true
                setScaleEnabled(true)
                
                // 边距设置
                setExtraOffsets(8f, 16f, 8f, 24f)
            }
        },
        update = { chart ->
            // 按日期分组计算收入和支出
            val dailyData = records
                .groupBy { record ->
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(record.date)
                }
                .mapValues { (_, dayRecords) ->
                    val income = dayRecords
                        .filter { it.type == TransactionType.INCOME }
                        .sumOf { it.amount }
                        .toFloat()
                    val expense = dayRecords
                        .filter { it.type == TransactionType.EXPENSE }
                        .sumOf { it.amount }
                        .toFloat()
                    Pair(income, expense)
                }
                .toList()
                .sortedBy { it.first }

            // 创建收入数据点
            val incomeEntries = dailyData.mapIndexed { index, (_, amounts) ->
                Entry(index.toFloat(), amounts.first)
            }

            // 创建支出数据点
            val expenseEntries = dailyData.mapIndexed { index, (_, amounts) ->
                Entry(index.toFloat(), amounts.second)
            }

            // 创建收入数据集
            val incomeDataSet = LineDataSet(incomeEntries, "收入").apply {
                color = incomeColor
                lineWidth = 2.5f
                setDrawCircles(true)
                circleRadius = 4f
                setCircleColor(incomeColor)
                valueTextColor = textColor
                valueTextSize = 12f
                setDrawFilled(true)
                fillColor = incomeColor
                fillAlpha = if (isDarkTheme) 40 else 50
            }

            // 创建支出数据集
            val expenseDataSet = LineDataSet(expenseEntries, "支出").apply {
                color = expenseColor
                lineWidth = 2.5f
                setDrawCircles(true)
                circleRadius = 4f
                setCircleColor(expenseColor)
                valueTextColor = textColor
                valueTextSize = 12f
                setDrawFilled(true)
                fillColor = expenseColor
                fillAlpha = if (isDarkTheme) 40 else 50
            }

            // 设置X轴标签
            chart.xAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return try {
                        dailyData[value.toInt()].first.substring(5) // 只显示MM-dd
                    } catch (e: Exception) {
                        ""
                    }
                }
            }

            // 更新图表数据
            chart.data = LineData(incomeDataSet, expenseDataSet)
            chart.invalidate()
        }
    )
}
