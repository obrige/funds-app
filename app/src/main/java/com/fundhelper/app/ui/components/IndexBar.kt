package com.fundhelper.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fundhelper.app.data.model.IndexDisplayItem
import com.fundhelper.app.ui.theme.DownGreen
import com.fundhelper.app.ui.theme.UpRed
import com.fundhelper.app.util.Constants
import com.fundhelper.app.util.formatPercent

private const val INDICES_PER_ROW = 4

@Composable
fun IndexBar(
    indices: List<IndexDisplayItem>,
    isEditing: Boolean,
    onRemoveIndex: (String) -> Unit,
    onAddIndex: (String, String, String, Int) -> Unit,
    onIndexClick: ((IndexDisplayItem) -> Unit)? = null
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Column {
        // 动态分块显示，每行 INDICES_PER_ROW 个，自动延展不限数量
        val chunks = indices.chunked(INDICES_PER_ROW)
        chunks.forEach { chunk ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                chunk.forEach { item ->
                    val color = if ((item.quote?.changeRate ?: 0.0) >= 0) UpRed else DownGreen
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .clickable(enabled = !isEditing) { onIndexClick?.invoke(item) }
                            .padding(horizontal = 6.dp, vertical = 8.dp)
                    ) {
                        if (isEditing) {
                            IconButton(
                                onClick = { onRemoveIndex(item.entity.secId) },
                                modifier = Modifier.size(16.dp).align(Alignment.TopEnd)
                            ) {
                                Icon(Icons.Default.Close, "删除", modifier = Modifier.size(12.dp))
                            }
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(item.entity.name, fontSize = 11.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                            Text(item.quote?.price?.toString() ?: "--", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
                            Text(item.quote?.changeRate?.formatPercent() ?: "--", fontSize = 11.sp, color = color)
                        }
                    }
                }
                // 补齐不足 INDICES_PER_ROW 的空白占位
                repeat(INDICES_PER_ROW - chunk.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        // 编辑模式：始终显示添加按钮（不限数量）
        if (isEditing) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { showAddDialog = true }
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("+ 添加指数", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }

    if (showAddDialog) {
        AddIndexDialog(
            existingIds = indices.map { it.entity.secId }.toSet(),
            onDismiss = { showAddDialog = false },
            onAdd = { secId, name, code, market ->
                onAddIndex(secId, name, code, market)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AddIndexDialog(
    existingIds: Set<String>,
    onDismiss: () -> Unit,
    onAdd: (String, String, String, Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加指数") },
        text = {
            LazyColumn {
                items(Constants.AVAILABLE_INDICES) { (secId, name, code) ->
                    val exists = existingIds.contains(secId)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !exists) {
                                val market = when {
                                    secId.startsWith("0.") -> 0
                                    secId.startsWith("100.") -> 100
                                    secId.startsWith("105.") -> 105
                                    secId.startsWith("113.") -> 113
                                    else -> 1
                                }
                                onAdd(secId, name, code, market)
                            }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(name, color = if (exists) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface)
                        if (exists) Text("已添加", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
