package com.yovinchen.bookkeeping.ui.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yovinchen.bookkeeping.model.Member

@Composable
fun MemberManagementDialog(
    onDismiss: () -> Unit,
    members: List<Member>,
    onAddMember: (String, String) -> Unit,
    onDeleteMember: (Member) -> Unit,
    onUpdateMember: (Member, String, String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedMember by remember { mutableStateOf<Member?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("成员管理") },
        text = {
            Column {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(members) { member ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = member.name,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                if (member.description.isNotEmpty()) {
                                    Text(
                                        text = member.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Row {
                                IconButton(onClick = { selectedMember = member }) {
                                    Icon(Icons.Default.Edit, "编辑成员")
                                }
                                IconButton(onClick = { onDeleteMember(member) }) {
                                    Icon(Icons.Default.Delete, "删除成员")
                                }
                            }
                        }
                        if (members.indexOf(member) < members.size - 1) {
                            HorizontalDivider()
                        }
                    }
                }
                
                Button(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Icon(Icons.Default.Add, "添加成员")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("添加成员")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )

    // 添加成员对话框
    if (showAddDialog) {
        MemberEditDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, description ->
                onAddMember(name, description)
                showAddDialog = false
            }
        )
    }

    // 编辑成员对话框
    selectedMember?.let { member ->
        MemberEditDialog(
            onDismiss = { selectedMember = null },
            onConfirm = { name, description ->
                onUpdateMember(member, name, description)
                selectedMember = null
            },
            initialName = member.name,
            initialDescription = member.description
        )
    }
}

@Composable
private fun MemberEditDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit,
    initialName: String = "",
    initialDescription: String = ""
) {
    var name by remember { mutableStateOf(initialName) }
    var description by remember { mutableStateOf(initialDescription) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialName.isEmpty()) "添加成员" else "编辑成员") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("成员名称") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("描述（可选）") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name.trim(), description.trim())
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
