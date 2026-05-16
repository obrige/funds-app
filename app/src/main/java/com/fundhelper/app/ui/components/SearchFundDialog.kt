package com.fundhelper.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.fundhelper.app.data.model.FundSearchItem

@Composable
fun SearchFundDialog(
    onDismiss: () -> Unit,
    onSearch: (String, (List<FundSearchItem>) -> Unit) -> Unit,
    onAdd: (String, String) -> Unit
) {
    var keyword by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<FundSearchItem>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.7f),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("添加基金", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 12.dp))

                OutlinedTextField(
                    value = keyword,
                    onValueChange = { keyword = it },
                    label = { Text("输入基金代码或名称") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = {
                            if (keyword.isNotBlank()) {
                                isSearching = true
                                onSearch(keyword) { result -> results = result; isSearching = false }
                            }
                        }) {
                            Icon(Icons.Default.Search, "搜索")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text("支持按名称、拼音、编码模糊搜索", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(8.dp))

                if (isSearching) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(results) { item ->
                        ListItem(
                            headlineContent = { Text(item.name, fontSize = 14.sp) },
                            supportingContent = { Text(item.code, fontSize = 12.sp) },
                            trailingContent = {
                                TextButton(onClick = { onAdd(item.code, item.name) }) { Text("添加") }
                            },
                            modifier = Modifier.clickable { onAdd(item.code, item.name) }
                        )
                        HorizontalDivider()
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("关闭") }
                }
            }
        }
    }
}
