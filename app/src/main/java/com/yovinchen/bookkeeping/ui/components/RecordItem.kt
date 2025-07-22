package com.yovinchen.bookkeeping.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.yovinchen.bookkeeping.model.BookkeepingRecord
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.background
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.graphics.graphicsLayer
import com.yovinchen.bookkeeping.model.Member
import com.yovinchen.bookkeeping.model.TransactionType
import com.yovinchen.bookkeeping.utils.IconManager
import java.text.SimpleDateFormat
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.ui.text.font.FontWeight
import com.yovinchen.bookkeeping.ui.theme.IncomeColor
import com.yovinchen.bookkeeping.ui.theme.ExpenseColor
import java.util.Locale

@Composable
fun RecordItem(
    record: BookkeepingRecord,
    onClick: () -> Unit = {},
    onDelete: () -> Unit = {},
    modifier: Modifier = Modifier,
    members: List<Member> = emptyList()
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val member = members.find { it.id == record.memberId }
    val categoryIcon = IconManager.getCategoryIconVector(record.category)
    
    // 添加滑动和缩放动画状态
    var offsetX by remember { mutableStateOf(0f) }
    val animatedOffset by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy
        )
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp, vertical = 4.dp)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(12.dp),
                clip = false
            )
            .graphicsLayer {
                translationX = animatedOffset
                scaleX = scale
                scaleY = scale
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { isPressed = true },
                    onDragEnd = {
                        isPressed = false
                        if (offsetX < -100) {
                            showDeleteDialog = true
                        }
                        offsetX = 0f
                    },
                    onDragCancel = {
                        isPressed = false
                        offsetX = 0f
                    }
                ) { _, dragAmount ->
                    offsetX = (offsetX + dragAmount).coerceIn(-200f, 0f)
                }
            }
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
            pressedElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (record.type == TransactionType.INCOME) {
                        IncomeColor.copy(alpha = 0.05f)
                    } else {
                        ExpenseColor.copy(alpha = 0.05f)
                    }
                )
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧分类图标
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (record.type == TransactionType.INCOME) {
                    IncomeColor.copy(alpha = 0.1f)
                } else {
                    ExpenseColor.copy(alpha = 0.1f)
                },
                modifier = Modifier.size(44.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (categoryIcon != null) {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = record.category,
                            modifier = Modifier.size(24.dp),
                            tint = if (record.type == TransactionType.INCOME) {
                                IncomeColor
                            } else {
                                ExpenseColor
                            }
                        )
                    }
                }
            }

            // 中间内容区域
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // 第一行：分类
                Text(
                    text = record.category,
                    style = MaterialTheme.typography.bodyLarge
                )
                
                // 第二行：时间 | 成员 | 详情
                Text(
                    text = buildString {
                        append(timeFormat.format(record.date))
                        append(" | ")
                        append(member?.name ?: "自己")
                        if (record.description.isNotEmpty()) {
                            append(" | ")
                            append(record.description)
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 右侧金额显示
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = String.format(Locale.getDefault(), "%.2f", record.amount),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (record.type == TransactionType.EXPENSE)
                        ExpenseColor
                    else
                        IncomeColor
                )
                Text(
                    text = if (record.type == TransactionType.EXPENSE) "支出" else "收入",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }

    if (showDeleteDialog) {
        AnimatedVisibility(
            visible = showDeleteDialog,
            enter = fadeIn(animationSpec = tween(300)) + scaleIn(
                initialScale = 0.8f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy
                )
            ),
            exit = fadeOut(animationSpec = tween(200)) + scaleOut(targetScale = 0.8f)
        ) {
            AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除这条记录吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
            )
        }
    }
}
