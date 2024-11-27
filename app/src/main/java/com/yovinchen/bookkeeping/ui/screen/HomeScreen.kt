package com.yovinchen.bookkeeping.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yovinchen.bookkeeping.model.BookkeepingRecord
import com.yovinchen.bookkeeping.model.TransactionType
import com.yovinchen.bookkeeping.ui.dialog.AddRecordDialog
import com.yovinchen.bookkeeping.ui.dialog.RecordEditDialog
import com.yovinchen.bookkeeping.viewmodel.HomeViewModel
import java.time.YearMonth
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier, viewModel: HomeViewModel = viewModel()
) {
    val filteredRecords by viewModel.filteredRecords.collectAsState()
    val totalIncome by viewModel.totalIncome.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedRecordType by viewModel.selectedRecordType.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedRecord by remember { mutableStateOf<BookkeepingRecord?>(null) }

    Scaffold(modifier = modifier.fillMaxSize(), floatingActionButton = {
        FloatingActionButton(onClick = { showAddDialog = true }) {
            Icon(Icons.Default.Add, contentDescription = "添加记录")
        }
    }, floatingActionButtonPosition = FabPosition.End, topBar = {
        TopAppBar(title = { Text("记账本") })
    }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // 顶部统计信息
            MonthlyStatistics(totalIncome = totalIncome,
                totalExpense = totalExpense,
                onIncomeClick = { viewModel.setSelectedRecordType(TransactionType.INCOME) },
                onExpenseClick = { viewModel.setSelectedRecordType(TransactionType.EXPENSE) },
                selectedType = selectedRecordType,
                onClearFilter = { viewModel.setSelectedRecordType(null) },
                selectedMonth = selectedMonth,
                onPreviousMonth = { viewModel.setSelectedMonth(selectedMonth.minusMonths(1)) },
                onNextMonth = { viewModel.setSelectedMonth(selectedMonth.plusMonths(1)) },
                onMonthSelected = { viewModel.setSelectedMonth(it) })

            // 记录列表
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                filteredRecords.forEach { (date, records) ->
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(12.dp),
                            tonalElevation = 2.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                // 日期标签
                                Text(
                                    text = SimpleDateFormat(
                                        "yyyy年MM月dd日 E", Locale.CHINESE
                                    ).format(date),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // 当天的记录
                                records.forEachIndexed { index, record ->
                                    RecordItem(record = record,
                                        onClick = { selectedRecord = record },
                                        onDelete = { viewModel.deleteRecord(record) })

                                    if (index < records.size - 1) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            thickness = 0.5.dp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // 当天统计
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    thickness = 0.5.dp
                                )

                                val dayIncome = records.filter { it.type == TransactionType.INCOME }
                                    .sumOf { it.amount }
                                val dayExpense =
                                    records.filter { it.type == TransactionType.EXPENSE }
                                        .sumOf { it.amount }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "收入: ¥%.2f".format(dayIncome),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "支出: ¥%.2f".format(dayExpense),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 添加记录对话框
        if (showAddDialog) {
            val selectedDateTime by viewModel.selectedDateTime.collectAsState()
            val selectedCategoryType by viewModel.selectedCategoryType.collectAsState()
            AddRecordDialog(onDismiss = {
                showAddDialog = false
                viewModel.resetSelectedDateTime()
            },
                onConfirm = { type, amount, category, description ->
                    viewModel.addRecord(type, amount, category, description)
                    showAddDialog = false
                },
                categories = categories,
                selectedType = selectedCategoryType,
                onTypeChange = viewModel::setSelectedCategoryType,
                selectedDateTime = selectedDateTime,
                onDateTimeSelected = viewModel::setSelectedDateTime
            )
        }

        // 编辑记录对话框
        selectedRecord?.let { record ->
            RecordEditDialog(record = record,
                categories = categories,
                onDismiss = { selectedRecord = null },
                onConfirm = { updatedRecord ->
                    viewModel.updateRecord(updatedRecord)
                    selectedRecord = null
                })
        }
    }
}

@Composable
fun MonthYearPickerDialog(
    selectedMonth: YearMonth, onMonthSelected: (YearMonth) -> Unit, onDismiss: () -> Unit
) {
    var currentYearMonth by remember { mutableStateOf(selectedMonth) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
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
                        Text("<")
                    }
                    Text(
                        text = "${currentYearMonth.year}年",
                        style = MaterialTheme.typography.titleMedium
                    )
                    IconButton(onClick = {
                        currentYearMonth = currentYearMonth.plusYears(1)
                    }) {
                        Text(">")
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

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
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
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.clickable { showMonthPicker = true })

                IconButton(onClick = onNextMonth) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "下个月")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 收入统计
                Column(modifier = Modifier
                    .weight(1f)
                    .clickable { onIncomeClick() }
                    .background(
                        if (selectedType == TransactionType.INCOME) MaterialTheme.colorScheme.primaryContainer
                        else Color.Transparent, RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp)) {
                    Text(
                        text = "收入", style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "¥${String.format("%.2f", totalIncome)}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // 支出统计
                Column(modifier = Modifier
                    .weight(1f)
                    .clickable { onExpenseClick() }
                    .background(
                        if (selectedType == TransactionType.EXPENSE) MaterialTheme.colorScheme.primaryContainer
                        else Color.Transparent, RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp)) {
                    Text(
                        text = "支出", style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "¥${String.format("%.2f", totalExpense)}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
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

@Composable
fun RecordItem(
    record: BookkeepingRecord,
    onClick: () -> Unit = {},
    onDelete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.category, style = MaterialTheme.typography.titleMedium
                )
                if (record.description.isNotEmpty()) {
                    Text(
                        text = record.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = SimpleDateFormat(
                        "yyyy-MM-dd HH:mm", Locale.getDefault()
                    ).format(record.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (record.type == TransactionType.EXPENSE) "-" else "+",
                    color = if (record.type == TransactionType.EXPENSE) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = String.format("%.2f", record.amount),
                    color = if (record.type == TransactionType.EXPENSE) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(end = 8.dp)
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
