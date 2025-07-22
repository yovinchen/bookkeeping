package com.yovinchen.bookkeeping.ui.screen

import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.background
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.Alignment
import com.yovinchen.bookkeeping.model.BookkeepingRecord
import com.yovinchen.bookkeeping.model.TransactionType
import com.yovinchen.bookkeeping.ui.components.MonthlyStatistics
import com.yovinchen.bookkeeping.ui.components.RecordItem
import com.yovinchen.bookkeeping.ui.dialog.AddRecordDialog
import com.yovinchen.bookkeeping.ui.dialog.RecordEditDialog
import com.yovinchen.bookkeeping.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedRecord by remember { mutableStateOf<BookkeepingRecord?>(null) }
    
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val filteredRecords by viewModel.filteredRecords.collectAsState()
    val categories by viewModel.categories.collectAsState(initial = emptyList())
    val members by viewModel.members.collectAsState(initial = emptyList())
    val totalIncome by viewModel.totalIncome.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()
    
    // 获取屏幕高度
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val headerHeight = screenHeight * 0.2f // 20% 的屏幕高度
    
    // LazyColumn 滑动状态
    val listState = rememberLazyListState()
    
    // 计算滑动偏移量
    val scrollOffset by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex > 0) {
                1f // 完全收起
            } else {
                val offset = listState.firstVisibleItemScrollOffset.toFloat()
                (offset / headerHeight.value).coerceIn(0f, 1f)
            }
        }
    }
    
    // 动画化的偏移量
    val animatedScrollOffset by animateFloatAsState(
        targetValue = scrollOffset,
        animationSpec = tween(
            durationMillis = 100,
            easing = LinearEasing
        )
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            AnimatedVisibility(
                visible = true,
                enter = scaleIn(
                    initialScale = 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ),
                exit = scaleOut(targetScale = 0f)
            ) {
                ExtendedFloatingActionButton(
                    onClick = { showAddDialog = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("记一笔") }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // 记录列表（背景层）
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = headerHeight + 16.dp,
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(
                    items = filteredRecords.toList(),
                    key = { _, (date, _) -> date }
                ) { index, (date, dayRecords) ->
                    val animationDelay = remember { (index * 30).coerceAtMost(300) }
                    var isVisible by remember { mutableStateOf(false) }
                    
                    LaunchedEffect(key1 = date) {
                        delay(animationDelay.toLong())
                        isVisible = true
                    }
                    
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(
                            animationSpec = tween(
                                durationMillis = 400,
                                easing = FastOutSlowInEasing
                            )
                        ) + slideInVertically(
                            initialOffsetY = { it / 4 },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        ) + scaleIn(
                            initialScale = 0.85f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        ),
                        exit = fadeOut(
                            animationSpec = tween(200)
                        ) + slideOutVertically(
                            targetOffsetY = { -it / 4 },
                            animationSpec = tween(200)
                        )
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .graphicsLayer {
                                    shadowElevation = 8.dp.toPx()
                                }
                                .animateContentSize(
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                ),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 4.dp
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.surface,
                                                MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                                            )
                                        )
                                    )
                                    .padding(20.dp)
                            ) {
                                // 日期标签
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.padding(end = 8.dp)
                                    ) {
                                        Text(
                                            text = SimpleDateFormat(
                                                "dd",
                                                Locale.CHINESE
                                            ).format(date),
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = SimpleDateFormat(
                                                "yyyy年MM月",
                                                Locale.CHINESE
                                            ).format(date),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = SimpleDateFormat(
                                                "EEEE",
                                                Locale.CHINESE
                                            ).format(date),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // 当天的记录
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    dayRecords.forEachIndexed { recordIndex, record ->
                                        RecordItem(
                                            record = record,
                                            onClick = { selectedRecord = record },
                                            onDelete = { viewModel.deleteRecord(record) },
                                            members = members
                                        )
                                        if (recordIndex < dayRecords.size - 1) {
                                            HorizontalDivider(
                                                modifier = Modifier.padding(vertical = 4.dp),
                                                color = MaterialTheme.colorScheme.surfaceVariant,
                                                thickness = 0.5.dp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // 顶部统计信息（悬浮层）
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(headerHeight)
                    .graphicsLayer {
                        alpha = 1f - animatedScrollOffset * 0.3f // 淡出效果更柔和
                        translationY = -animatedScrollOffset * headerHeight.value * 0.5f
                        scaleY = 1f - animatedScrollOffset * 0.5f
                        transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0f)
                    }
                    .background(MaterialTheme.colorScheme.background)
            ) {
                if (animatedScrollOffset < 0.9f) {
                    MonthlyStatistics(
                        totalIncome = totalIncome,
                        totalExpense = totalExpense,
                        selectedType = null,
                        onIncomeClick = { viewModel.setSelectedRecordType(TransactionType.INCOME) },
                        onExpenseClick = { viewModel.setSelectedRecordType(TransactionType.EXPENSE) },
                        onClearFilter = { viewModel.setSelectedRecordType(null) },
                        selectedMonth = selectedMonth,
                        onPreviousMonth = { viewModel.moveMonth(false) },
                        onNextMonth = { viewModel.moveMonth(true) },
                        onMonthSelected = { viewModel.setSelectedMonth(it) }
                    )
                }
            }
        }
    }

    // 添加记录对话框
    if (showAddDialog) {
        AddRecordDialog(
            categories = categories,
            members = members,
            onDismiss = { showAddDialog = false },
            onConfirm = { amount, category, description, date, type, memberId ->
                viewModel.addRecord(amount, category, description, date, type, memberId)
                showAddDialog = false
            }
        )
    }

    // 编辑记录对话框
    selectedRecord?.let { record ->
        RecordEditDialog(
            record = record,
            categories = categories,
            members = members,
            onDismiss = { selectedRecord = null },
            onConfirm = { updatedRecord ->
                viewModel.updateRecord(updatedRecord)
                selectedRecord = null
            }
        )
    }
}