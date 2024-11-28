package com.yovinchen.bookkeeping.ui.components

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate

@Composable
fun CategoryPieChart(
    categoryData: List<Pair<String, Float>>,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = isSystemInDarkTheme()
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp),
        factory = { context ->
            PieChart(context).apply {
                description.isEnabled = false
                setUsePercentValues(true)
                setDrawEntryLabels(true)

                // 禁用图例显示
                legend.isEnabled = false

                isDrawHoleEnabled = true
                holeRadius = 40f
                setHoleColor(AndroidColor.TRANSPARENT)
                setTransparentCircleRadius(45f)

                // 设置标签文字颜色
                setEntryLabelColor(textColor)
                setEntryLabelTextSize(12f)

                // 设置中心文字颜色跟随主题
                setCenterTextColor(textColor)
            }
        },
        update = { chart ->
            val entries = categoryData.map { (category, amount) ->
                PieEntry(amount, category)
            }

            val dataSet = PieDataSet(entries, "").apply {  // 将标题设为空字符串
                colors = ColorTemplate.MATERIAL_COLORS.toList()
                valueTextSize = 14f
                valueFormatter = PercentFormatter(chart)
                valueTextColor = textColor
                setDrawValues(true)
            }

            val pieData = PieData(dataSet)
            chart.data = pieData
            chart.invalidate()
        }
    )
}
