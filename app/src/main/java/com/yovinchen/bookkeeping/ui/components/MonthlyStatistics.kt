package com.yovinchen.bookkeeping.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.yovinchen.bookkeeping.model.TransactionType
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import com.yovinchen.bookkeeping.ui.theme.IncomeColor
import com.yovinchen.bookkeeping.ui.theme.ExpenseColor
import com.yovinchen.bookkeeping.ui.theme.BalancePositive
import com.yovinchen.bookkeeping.ui.theme.BalanceNegative
import java.time.YearMonth
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.ui.text.style.TextOverflow
import kotlin.math.abs
import java.util.Locale

// 格式化金额显示，适应不同数量级
private fun formatAmount(amount: Float): String {
    val absAmount = abs(amount)
    return when {
        absAmount >= 100000 -> {
            // 十万以上显示为万为单位
            val wan = amount / 10000
            "¥${String.format(Locale.getDefault(), "%.1f", wan)}万"
        }
        absAmount >= 10000 -> {
            // 万级显示一位小数
            "¥${String.format(Locale.getDefault(), "%.1f", amount)}"
        }
        else -> {
            // 千级以下显示两位小数
            "¥${String.format(Locale.getDefault(), "%.2f", amount)}"
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MonthYearPickerDialog(
    selectedMonth: YearMonth, onMonthSelected: (YearMonth) -> Unit, onDismiss: () -> Unit
) {
    var currentYearMonth by remember { mutableStateOf(selectedMonth) }
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isVisible = true
    }

    Dialog(onDismissRequest = {
        isVisible = false
        onDismiss()
    }) {
        AnimatedVisibility(
            visible = isVisible,
            enter = scaleIn(
                initialScale = 0.8f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ) + fadeIn(animationSpec = tween(300)),
            exit = scaleOut(targetScale = 0.8f) + fadeOut(animationSpec = tween(200))
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy
                        )
                    ),
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                Text(
                    text = "选择年月",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // 年份选择
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        currentYearMonth = currentYearMonth.minusYears(1)
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "上一年",
                            modifier = Modifier.animateContentSize()
                        )
                    }
                    AnimatedContent(
                        targetState = currentYearMonth.year,
                        transitionSpec = {
                            if (targetState > initialState) {
                                slideInVertically { -it } + fadeIn() togetherWith
                                slideOutVertically { it } + fadeOut()
                            } else {
                                slideInVertically { it } + fadeIn() togetherWith
                                slideOutVertically { -it } + fadeOut()
                            }
                        }
                    ) { year ->
                        Text(
                            text = "${year}年",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    IconButton(onClick = {
                        currentYearMonth = currentYearMonth.plusYears(1)
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "下一年",
                            modifier = Modifier.animateContentSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 月份网格
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3), modifier = Modifier.height(200.dp)
                ) {
                    items(12) { index ->
                        val month = index + 1
                        val isSelected = month == currentYearMonth.monthValue

                        Surface(
                            modifier = Modifier
                                .padding(4.dp)
                                .aspectRatio(1.5f)
                                .clickable {
                                    currentYearMonth = YearMonth.of(currentYearMonth.year, month)
                                },
                            shape = MaterialTheme.shapes.small,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surface
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = "${month}月",
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // 按钮行
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        onMonthSelected(currentYearMonth)
                        onDismiss()
                    }) {
                        Text("确定")
                    }
                }
            }
        }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun MonthlyStatistics(
    totalIncome: Double,
    totalExpense: Double,
    onIncomeClick: () -> Unit,
    onExpenseClick: () -> Unit,
    selectedType: TransactionType?,
    onClearFilter: () -> Unit,
    selectedMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onMonthSelected: (YearMonth) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMonthPicker by remember { mutableStateOf(false) }
    val balance = totalIncome - totalExpense
    
    // 添加动画效果
    val animatedIncome by animateFloatAsState(
        targetValue = totalIncome.toFloat(),
        animationSpec = tween(600, easing = FastOutSlowInEasing)
    )
    val animatedExpense by animateFloatAsState(
        targetValue = totalExpense.toFloat(),
        animationSpec = tween(600, easing = FastOutSlowInEasing)
    )
    val animatedBalance by animateFloatAsState(
        targetValue = balance.toFloat(),
        animationSpec = tween(600, easing = FastOutSlowInEasing)
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                clip = false
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                        )
                    )
                )
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            // 月份选择器
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousMonth) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "上个月")
                }

                Text(text = "${selectedMonth.year}年${selectedMonth.monthValue}月",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.clickable { showMonthPicker = true })

                IconButton(onClick = onNextMonth) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "下个月")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 支出统计
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onExpenseClick() },
                    shape = RoundedCornerShape(12.dp),
                    color = if (selectedType == TransactionType.EXPENSE) 
                        ExpenseColor.copy(alpha = 0.15f)
                    else 
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                                contentDescription = "支出",
                                modifier = Modifier.size(14.dp),
                                tint = ExpenseColor
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "支出",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = formatAmount(animatedExpense),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = ExpenseColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                // 收入统计
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onIncomeClick() },
                    shape = RoundedCornerShape(12.dp),
                    color = if (selectedType == TransactionType.INCOME) 
                        IncomeColor.copy(alpha = 0.15f)
                    else 
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                contentDescription = "收入",
                                modifier = Modifier.size(14.dp),
                                tint = IncomeColor
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "收入",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = formatAmount(animatedIncome),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = IncomeColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))
                // 结余统计
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onClearFilter() },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = "结余",
                                modifier = Modifier.size(14.dp),
                                tint = if (animatedBalance >= 0) BalancePositive else BalanceNegative
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "结余",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = formatAmount(animatedBalance),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (animatedBalance >= 0) BalancePositive else BalanceNegative,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            if (selectedType != null) {
                TextButton(
                    onClick = onClearFilter, modifier = Modifier.align(Alignment.End)
                ) {
                    Text("清除筛选")
                }
            }
        }
    }

    if (showMonthPicker) {
        MonthYearPickerDialog(selectedMonth = selectedMonth,
            onMonthSelected = onMonthSelected,
            onDismiss = { showMonthPicker = false })
    }
}
