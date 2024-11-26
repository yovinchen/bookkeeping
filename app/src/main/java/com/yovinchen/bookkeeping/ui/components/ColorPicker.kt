package com.yovinchen.bookkeeping.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp

val predefinedColors = listOf(
    Color(0xFF6200EE), // 默认紫色
    Color(0xFF3700B3),
    Color(0xFF03DAC6),
    Color(0xFF018786),
    Color(0xFFE91E63), // Pink
    Color(0xFFF44336), // Red
    Color(0xFFFF9800), // Orange
    Color(0xFFFFEB3B), // Yellow
    Color(0xFF4CAF50), // Green
    Color(0xFF2196F3), // Blue
    Color(0xFF9C27B0), // Purple
    Color(0xFF795548), // Brown
)

@Composable
fun ColorPicker(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit
) {
    var showCustomColorPicker by remember { mutableStateOf(false) }
    
    Column(modifier = Modifier.padding(16.dp)) {
        // 预定义颜色网格
        LazyVerticalGrid(
            columns = GridCells.Fixed(6),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            items(predefinedColors) { color ->
                ColorItem(
                    color = color,
                    isSelected = selectedColor == color,
                    onClick = { onColorSelected(color) }
                )
            }
        }

        // 自定义颜色按钮
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showCustomColorPicker = !showCustomColorPicker }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "自定义颜色",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
        }

        // 自定义颜色选择器
        if (showCustomColorPicker) {
            CustomColorPicker(
                initialColor = selectedColor,
                onColorSelected = onColorSelected
            )
        }
    }
}

@Composable
private fun ColorItem(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(color)
            .clickable(onClick = onClick)
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = if (color.luminance() > 0.5f) Color.Black else Color.White
            )
        }
    }
}

@Composable
private fun CustomColorPicker(
    initialColor: Color,
    onColorSelected: (Color) -> Unit
) {
    var red by remember { mutableStateOf(initialColor.red) }
    var green by remember { mutableStateOf(initialColor.green) }
    var blue by remember { mutableStateOf(initialColor.blue) }

    Column(modifier = Modifier.padding(8.dp)) {
        ColorSlider("红色", red) { red = it }
        ColorSlider("绿色", green) { green = it }
        ColorSlider("蓝色", blue) { blue = it }

        // 预览颜色
        val currentColor = Color(red, green, blue)
        Box(
            modifier = Modifier
                .padding(top = 16.dp)
                .size(48.dp)
                .clip(CircleShape)
                .background(currentColor)
                .clickable { onColorSelected(currentColor) }
        )
    }
}

@Composable
private fun ColorSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Column {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..1f,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}
