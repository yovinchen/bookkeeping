package com.yovinchen.bookkeeping.ui.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.yovinchen.bookkeeping.utils.IconManager

@Composable
fun IconPickerDialog(
    onDismiss: () -> Unit,
    onIconSelected: (Int) -> Unit,
    selectedIcon: Int? = null,
    isMemberIcon: Boolean = false,
    title: String = "选择图标"
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val icons = if (isMemberIcon) {
                    IconManager.getAllMemberIcons()
                } else {
                    IconManager.getAllCategoryIcons()
                }

                items(icons) { iconResId ->
                    Icon(
                        imageVector = ImageVector.vectorResource(id = iconResId),
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .clickable { onIconSelected(iconResId) },
                        tint = if (selectedIcon == iconResId) MaterialTheme.colorScheme.primary else Color.Unspecified
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
