package com.yovinchen.bookkeeping.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yovinchen.bookkeeping.model.*
import com.yovinchen.bookkeeping.ui.dialog.BudgetEditDialog
import com.yovinchen.bookkeeping.viewmodel.BudgetViewModel
import com.yovinchen.bookkeeping.viewmodel.HomeViewModel
import com.yovinchen.bookkeeping.viewmodel.MemberViewModel
import java.text.NumberFormat
import java.util.Locale

/**
 * 预算管理界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel = viewModel(),
    homeViewModel: HomeViewModel = viewModel(),
    memberViewModel: MemberViewModel = viewModel()
) {
    val budgetStatuses by viewModel.activeBudgetStatuses.collectAsState()
    val totalBudgetStatus by viewModel.totalBudgetStatus.collectAsState()
    val categoryBudgetStatuses by viewModel.categoryBudgetStatuses.collectAsState()
    val memberBudgetStatuses by viewModel.memberBudgetStatuses.collectAsState()
    val showBudgetDialog by viewModel.showBudgetDialog.collectAsState()
    val editingBudget by viewModel.editingBudget.collectAsState()
    
    val categories by homeViewModel.categories.collectAsState()
    val members by memberViewModel.allMembers.collectAsState(initial = emptyList())
    
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("总览", "分类预算", "成员预算")
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 页面标题
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "预算管理",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            // 添加预算按钮
            IconButton(
                onClick = { viewModel.showEditBudgetDialog() }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "添加预算",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 总预算概览卡片
        totalBudgetStatus?.let { status ->
            BudgetOverviewCard(status)
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Tab 选择器
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Tab 内容
        when (selectedTab) {
            0 -> BudgetOverviewTab(budgetStatuses, viewModel)
            1 -> CategoryBudgetTab(categoryBudgetStatuses, viewModel)
            2 -> MemberBudgetTab(memberBudgetStatuses, viewModel)
        }
    }
    
    // 预算编辑对话框
    if (showBudgetDialog) {
        BudgetEditDialog(
            budget = editingBudget,
            categories = categories.filter { it.type == TransactionType.EXPENSE },
            members = members,
            onDismiss = { viewModel.hideBudgetDialog() },
            onConfirm = { budget ->
                if (editingBudget == null) {
                    viewModel.createBudget(
                        type = budget.type,
                        amount = budget.amount,
                        categoryName = budget.categoryName,
                        memberId = budget.memberId,
                        alertThreshold = budget.alertThreshold
                    )
                } else {
                    viewModel.updateBudget(budget)
                }
                viewModel.hideBudgetDialog()
            }
        )
    }
}

/**
 * 预算概览卡片
 */
@Composable
private fun BudgetOverviewCard(budgetStatus: BudgetStatus) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "本月总预算",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = formatCurrency(budgetStatus.budget.amount),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 进度条
            LinearProgressIndicator(
                progress = budgetStatus.percentage.toFloat().coerceIn(0f, 1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = when {
                    budgetStatus.isOverBudget -> MaterialTheme.colorScheme.error
                    budgetStatus.isNearLimit -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.primary
                }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "已使用",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(budgetStatus.spent),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "剩余",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(budgetStatus.remaining),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = when {
                            budgetStatus.isOverBudget -> MaterialTheme.colorScheme.error
                            budgetStatus.isNearLimit -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.primary
                        }
                    )
                }
            }
            
            if (budgetStatus.isOverBudget) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "⚠️ 已超出预算 ${formatCurrency(kotlin.math.abs(budgetStatus.remaining))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            } else if (budgetStatus.isNearLimit) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "⚠️ 接近预算限制",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * 预算总览标签页
 */
@Composable
private fun BudgetOverviewTab(
    budgetStatuses: List<BudgetStatus>,
    viewModel: BudgetViewModel
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(budgetStatuses) { status ->
            BudgetItem(
                budgetStatus = status,
                onClick = { viewModel.showEditBudgetDialog(status.budget) },
                onToggleEnabled = { viewModel.toggleBudgetEnabled(status.budget) }
            )
        }
    }
}

/**
 * 分类预算标签页
 */
@Composable
private fun CategoryBudgetTab(
    categoryBudgetStatuses: List<BudgetStatus>,
    viewModel: BudgetViewModel
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categoryBudgetStatuses) { status ->
            BudgetItem(
                budgetStatus = status,
                onClick = { viewModel.showEditBudgetDialog(status.budget) },
                onToggleEnabled = { viewModel.toggleBudgetEnabled(status.budget) }
            )
        }
    }
}

/**
 * 成员预算标签页
 */
@Composable
private fun MemberBudgetTab(
    memberBudgetStatuses: List<BudgetStatus>,
    viewModel: BudgetViewModel
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(memberBudgetStatuses) { status ->
            BudgetItem(
                budgetStatus = status,
                onClick = { viewModel.showEditBudgetDialog(status.budget) },
                onToggleEnabled = { viewModel.toggleBudgetEnabled(status.budget) }
            )
        }
    }
}

/**
 * 预算项目组件
 */
@Composable
private fun BudgetItem(
    budgetStatus: BudgetStatus,
    onClick: () -> Unit,
    onToggleEnabled: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (budgetStatus.budget.isEnabled) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = when (budgetStatus.budget.type) {
                        BudgetType.TOTAL -> "总预算"
                        BudgetType.CATEGORY -> budgetStatus.budget.categoryName ?: "未知分类"
                        BudgetType.MEMBER -> "成员预算"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "预算: ${formatCurrency(budgetStatus.budget.amount)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "已用: ${formatCurrency(budgetStatus.spent)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = when {
                            budgetStatus.isOverBudget -> MaterialTheme.colorScheme.error
                            budgetStatus.isNearLimit -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LinearProgressIndicator(
                    progress = budgetStatus.percentage.toFloat().coerceIn(0f, 1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = when {
                        budgetStatus.isOverBudget -> MaterialTheme.colorScheme.error
                        budgetStatus.isNearLimit -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
            }
            
            IconButton(onClick = onToggleEnabled) {
                Icon(
                    imageVector = if (budgetStatus.budget.isEnabled) {
                        Icons.Default.CheckCircle
                    } else {
                        Icons.Default.Cancel
                    },
                    contentDescription = if (budgetStatus.budget.isEnabled) "禁用" else "启用",
                    tint = if (budgetStatus.budget.isEnabled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

/**
 * 格式化货币
 */
private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale.CHINA)
    return format.format(amount)
}