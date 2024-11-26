package com.yovinchen.bookkeeping.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yovinchen.bookkeeping.model.BookkeepingRecord
import com.yovinchen.bookkeeping.model.TransactionType
import com.yovinchen.bookkeeping.ui.dialog.AddRecordDialog
import com.yovinchen.bookkeeping.ui.dialog.CategoryManagementDialog
import com.yovinchen.bookkeeping.ui.dialog.RecordEditDialog
import com.yovinchen.bookkeeping.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
) {
    val records by viewModel.filteredRecords.collectAsState()
    val totalIncome by viewModel.totalIncome.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedType by viewModel.selectedCategoryType.collectAsState()
    val selectedRecordType by viewModel.selectedRecordType.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var selectedRecord by remember { mutableStateOf<BookkeepingRecord?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加记录")
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        topBar = {
            TopAppBar(
                title = { Text("记账本") },
                actions = {
                    IconButton(onClick = { showCategoryDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "类别管理")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 顶部统计信息
            MonthlyStatistics(
                totalIncome = totalIncome,
                totalExpense = totalExpense,
                onIncomeClick = { viewModel.setSelectedRecordType(TransactionType.INCOME) },
                onExpenseClick = { viewModel.setSelectedRecordType(TransactionType.EXPENSE) },
                selectedType = selectedRecordType,
                onClearFilter = { viewModel.setSelectedRecordType(null) }
            )

            // 记录列表
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(records) { record ->
                    RecordItem(
                        record = record,
                        onClick = { selectedRecord = record },
                        onDelete = { viewModel.deleteRecord(record) }
                    )
                }
            }
        }

        // 添加记录对话框
        if (showAddDialog) {
            val selectedDateTime by viewModel.selectedDateTime.collectAsState()
            AddRecordDialog(
                onDismiss = {
                    showAddDialog = false
                    viewModel.resetSelectedDateTime()
                },
                onConfirm = { type, amount, category, description ->
                    viewModel.addRecord(type, amount, category, description)
                    showAddDialog = false
                },
                categories = categories,
                selectedType = selectedType,
                onTypeChange = { viewModel.setSelectedCategoryType(it) },
                selectedDateTime = selectedDateTime,
                onDateTimeSelected = { viewModel.setSelectedDateTime(it) }
            )
        }

        // 类别管理对话框
        if (showCategoryDialog) {
            CategoryManagementDialog(
                onDismiss = { showCategoryDialog = false },
                categories = categories,
                onAddCategory = { name, type -> viewModel.addCategory(name, type) },
                onDeleteCategory = { category -> viewModel.deleteCategory(category) },
                onUpdateCategory = { category, newName -> viewModel.updateCategory(category, newName) },
                selectedType = selectedType,
                onTypeChange = { viewModel.setSelectedCategoryType(it) }
            )
        }

        // 编辑记录对话框
        selectedRecord?.let { record ->
            RecordEditDialog(
                record = record,
                categories = categories,
                onDismiss = { selectedRecord = null },
                onConfirm = { updatedRecord ->
                    viewModel.updateRecord(updatedRecord)
                    selectedRecord = null
                }
            )
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
    modifier: Modifier = Modifier
) {
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
            Text(
                text = "本月统计",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 收入统计
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onIncomeClick() }
                        .background(
                            if (selectedType == TransactionType.INCOME)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp)
                ) {
                    Text(
                        text = "收入",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "¥${String.format("%.2f", totalIncome)}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // 支出统计
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onExpenseClick() }
                        .background(
                            if (selectedType == TransactionType.EXPENSE)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp)
                ) {
                    Text(
                        text = "支出",
                        style = MaterialTheme.typography.titleMedium
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
                    onClick = onClearFilter,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("清除筛选")
                }
            }
        }
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
                    text = record.category,
                    style = MaterialTheme.typography.titleMedium
                )
                if (record.description.isNotEmpty()) {
                    Text(
                        text = record.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                        .format(record.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (record.type == TransactionType.EXPENSE) "-" else "+",
                    color = if (record.type == TransactionType.EXPENSE)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = String.format("%.2f", record.amount),
                    color = if (record.type == TransactionType.EXPENSE)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.primary,
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
