package com.yovinchen.bookkeeping.ui.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.yovinchen.bookkeeping.model.Member
import com.yovinchen.bookkeeping.utils.IconManager

@Composable
fun MemberManagementDialog(
    onDismiss: () -> Unit,
    members: List<Member>,
    onAddMember: (String, String, Int?) -> Unit,
    onDeleteMember: (Member) -> Unit,
    onUpdateMember: (Member, String, String, Int?) -> Unit
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                // 显示成员图标
                                if (member.icon != null) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(member.icon),
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp),
                                        tint = Color.Unspecified
                                    )
                                } else {
                                    IconManager.getMemberIconVector(member.name)?.let { icon ->
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            modifier = Modifier.size(24.dp),
                                            tint = Color.Unspecified
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Column {
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
                            }

                            Row {
                                IconButton(onClick = { selectedMember = member }) {
                                    Icon(Icons.Default.Edit, contentDescription = "编辑")
                                }
                                IconButton(onClick = { onDeleteMember(member) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "删除")
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("添加成员")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("完成")
            }
        }
    )

    if (showAddDialog) {
        MemberEditDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, description, iconResId ->
                onAddMember(name, description, iconResId)
                showAddDialog = false
            }
        )
    }

    selectedMember?.let { member ->
        MemberEditDialog(
            onDismiss = { selectedMember = null },
            onConfirm = { name, description, iconResId ->
                onUpdateMember(member, name, description, iconResId)
                selectedMember = null
            },
            initialName = member.name,
            initialDescription = member.description,
            initialIcon = member.icon
        )
    }
}

@Composable
private fun MemberEditDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int?) -> Unit,
    initialName: String = "",
    initialDescription: String = "",
    initialIcon: Int? = null
) {
    var name by remember { mutableStateOf(initialName) }
    var description by remember { mutableStateOf(initialDescription) }
    var selectedIcon by remember { mutableStateOf(initialIcon) }
    var showIconPicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialName.isEmpty()) "添加成员" else "编辑成员") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("名称") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("描述") },
                    modifier = Modifier.fillMaxWidth()
                )

                // 图标选择按钮
                Button(
                    onClick = { showIconPicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    selectedIcon?.let { iconResId ->
                        Icon(
                            imageVector = ImageVector.vectorResource(iconResId),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = Color.Unspecified
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(if (selectedIcon == null) "选择图标" else "更改图标")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name, description, selectedIcon)
                    }
                }
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

    if (showIconPicker) {
        IconPickerDialog(
            onDismiss = { showIconPicker = false },
            onIconSelected = { 
                selectedIcon = it
                showIconPicker = false
            },
            selectedIcon = selectedIcon,
            isMemberIcon = true,
            title = "选择成员图标"
        )
    }
}
