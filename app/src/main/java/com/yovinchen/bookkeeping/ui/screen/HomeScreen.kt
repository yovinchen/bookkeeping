package com.yovinchen.bookkeeping.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.Surface
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
import com.yovinchen.bookkeeping.model.BookkeepingRecord
import com.yovinchen.bookkeeping.model.TransactionType
import com.yovinchen.bookkeeping.ui.components.MonthlyStatistics
import com.yovinchen.bookkeeping.ui.components.RecordItem
import com.yovinchen.bookkeeping.ui.dialog.AddRecordDialog
import com.yovinchen.bookkeeping.ui.dialog.RecordEditDialog
import com.yovinchen.bookkeeping.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.Locale

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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("记一笔") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // 顶部统计信息
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

            // 记录列表
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredRecords.size) { index ->
                    val (date, dayRecords) = filteredRecords.toList()[index]
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // 日期标签
                            Text(
                                text = SimpleDateFormat(
                                    "yyyy年MM月dd日 E",
                                    Locale.CHINESE
                                ).format(date),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

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
