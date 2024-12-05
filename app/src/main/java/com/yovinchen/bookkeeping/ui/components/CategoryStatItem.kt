package com.yovinchen.bookkeeping.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yovinchen.bookkeeping.model.CategoryStat
import com.yovinchen.bookkeeping.model.MemberStat
import java.text.NumberFormat
import java.util.*

@Composable
fun CategoryStatItem(
    stat: Any,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val name = when (stat) {
        is CategoryStat -> stat.category
        is MemberStat -> stat.member
        else -> return
    }
    
    val amount = when (stat) {
        is CategoryStat -> stat.amount
        is MemberStat -> stat.amount
        else -> return
    }
    
    val count = when (stat) {
        is CategoryStat -> stat.count
        is MemberStat -> stat.count
        else -> return
    }
    
    val percentage = when (stat) {
        is CategoryStat -> stat.percentage
        is MemberStat -> stat.percentage
        else -> return
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
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
                    text = name,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "${count}笔 · ${String.format("%.1f%%", percentage)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = NumberFormat.getCurrencyInstance(Locale.CHINA).format(amount),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
