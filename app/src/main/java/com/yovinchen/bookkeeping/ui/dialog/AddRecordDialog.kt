@file:OptIn(ExperimentalMaterial3Api::class)

package com.yovinchen.bookkeeping.ui.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yovinchen.bookkeeping.ui.components.AnimatedDialog
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
import com.yovinchen.bookkeeping.ui.theme.ExpenseColor
import com.yovinchen.bookkeeping.ui.theme.IncomeColor
import com.yovinchen.bookkeeping.model.Category
import com.yovinchen.bookkeeping.model.Member
import com.yovinchen.bookkeeping.model.TransactionType
import com.yovinchen.bookkeeping.ui.components.DateTimePicker
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

@Composable
fun AddRecordDialog(
    categories: List<Category>,
    members: List<Member>,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, category: String, description: String, date: Date, type: TransactionType, memberId: Int?) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var memberExpanded by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    
    // 找到默认成员（"自己"）
    val defaultMember = remember(members) { 
        members.find { it.name == "自己" } 
    }
    var currentSelectedMember by remember(defaultMember) { 
        mutableStateOf(defaultMember)
    }
    
    // 设置默认分类为"餐饮"
    var selectedCategory by remember { 
        mutableStateOf(categories.find { it.type == selectedType && it.name == "餐饮" }?.name ?: categories.firstOrNull { it.type == selectedType }?.name ?: "")
    }
    
    var selectedDateTime by remember {
        mutableStateOf(LocalDateTime.now())
    }

    // 当类型改变时更新分类
    LaunchedEffect(selectedType) {
        selectedCategory = categories.find { it.type == selectedType && it.name == "餐饮" }?.name 
            ?: categories.firstOrNull { it.type == selectedType }?.name 
            ?: ""
    }

    AnimatedDialog(
        visible = true,
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(20.dp),
                    clip = false
                ),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
            ) {
                Text(
                    text = "添加记录",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 收入/支出选择
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + expandVertically()
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            FilterChip(
                                selected = selectedType == TransactionType.EXPENSE,
                                onClick = { selectedType = TransactionType.EXPENSE },
                                label = { Text("支出") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ExpenseColor.copy(alpha = 0.2f),
                                    selectedLabelColor = ExpenseColor
                                )
                            )
                            FilterChip(
                                selected = selectedType == TransactionType.INCOME,
                                onClick = { selectedType = TransactionType.INCOME },
                                label = { Text("收入") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = IncomeColor.copy(alpha = 0.2f),
                                    selectedLabelColor = IncomeColor
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("金额") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("类别") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.filter { it.type == selectedType }.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    selectedCategory = category.name
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                ExposedDropdownMenuBox(
                    expanded = memberExpanded,
                    onExpandedChange = { memberExpanded = it }
                ) {
                    OutlinedTextField(
                        value = currentSelectedMember?.name ?: "选择成员",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("成员") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = memberExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = memberExpanded,
                        onDismissRequest = { memberExpanded = false }
                    ) {
                        members.forEach { member ->
                            DropdownMenuItem(
                                text = { Text(member.name) },
                                onClick = {
                                    currentSelectedMember = member
                                    memberExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                DateTimePicker(
                    selectedDateTime = selectedDateTime,
                    onDateTimeSelected = { selectedDateTime = it },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("备注") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amountValue = amount.toDoubleOrNull()
                            if (amountValue != null) {
                                onConfirm(
                                    amountValue,
                                    selectedCategory,
                                    description,
                                    Date.from(
                                        selectedDateTime.atZone(ZoneId.systemDefault()).toInstant()
                                    ),
                                    selectedType,
                                    currentSelectedMember?.id
                                )
                            }
                        },
                        enabled = amount.isNotEmpty() && selectedCategory.isNotEmpty()
                    ) {
                        Text("确定")
                    }
                }
            }
        }
    }
}
