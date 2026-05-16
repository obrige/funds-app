package com.fundhelper.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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

@Composable
fun IndexBar(
    indices: List<IndexDisplayItem>,
    isEditing: Boolean,
    onRemoveIndex: (String) -> Unit,
    onAddIndex: (String, String, String, Int) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        indices.forEach { item ->
            val color = if ((item.quote?.changeRate ?: 0.0) >= 0) UpRed else DownGreen
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .clickable(enabled = !isEditing) { }
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

        if (isEditing && indices.size < 4) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { showAddDialog = true }
                    .padding(horizontal = 6.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("+ 添加", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
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
            Column {
                Constants.AVAILABLE_INDICES.forEach { (secId, name, code) ->
                    val exists = existingIds.contains(secId)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !exists) {
                                val market = if (secId.startsWith("0.")) 0 else 1
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
