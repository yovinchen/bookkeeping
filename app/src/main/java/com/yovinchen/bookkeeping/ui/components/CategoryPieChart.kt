package com.yovinchen.bookkeeping.ui.components

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate

@Composable
fun CategoryPieChart(
    categoryData: List<Pair<String, Float>>,
    memberData: List<Pair<String, Float>>,
    currentViewMode: Boolean = false, // false 为分类视图，true 为成员视图
    modifier: Modifier = Modifier,
    onCategoryClick: (String) -> Unit = {}
) {
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val data = if (currentViewMode) memberData else categoryData

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp),
        factory = { context ->
            PieChart(context).apply {
                description.isEnabled = false
                setUsePercentValues(true)
                setDrawEntryLabels(true)
                legend.isEnabled = false
                isDrawHoleEnabled = true
                holeRadius = 40f
                setHoleColor(AndroidColor.TRANSPARENT)
                setTransparentCircleRadius(45f)
                setEntryLabelColor(textColor)
                setEntryLabelTextSize(12f)
                setCenterTextColor(textColor)

                setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                    override fun onValueSelected(e: Entry?, h: Highlight?) {
                        e?.let {
                            if (it is PieEntry) {
                                onCategoryClick(it.label ?: return)
                            }
                        }
                    }

                    override fun onNothingSelected() {}
                })
            }
        },
        update = { chart ->
            val entries = data.map { (label, amount) ->
                PieEntry(amount, label)
            }

            val dataSet = PieDataSet(entries, "").apply {
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
