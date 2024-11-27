package com.yovinchen.bookkeeping.ui.components

import android.graphics.Color
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.PieChart
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
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp),
        factory = { context ->
            PieChart(context).apply {
                description.isEnabled = false
                setUsePercentValues(true)
                setDrawEntryLabels(true)
                legend.isEnabled = true
                isDrawHoleEnabled = true
                holeRadius = 40f
                setHoleColor(Color.TRANSPARENT)
                setTransparentCircleRadius(45f)
            }
        },
        update = { chart ->
            val entries = categoryData.map { (category, amount) ->
                PieEntry(amount, category)
            }

            val dataSet = PieDataSet(entries, "分类占比").apply {
                colors = ColorTemplate.MATERIAL_COLORS.toList()
                valueTextSize = 14f
                valueFormatter = PercentFormatter(chart)
                valueTextColor = Color.WHITE
                setDrawValues(true)
            }

            val pieData = PieData(dataSet)
            chart.data = pieData
            chart.invalidate()
        }
    )
}
