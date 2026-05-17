package com.fundhelper.app.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val prefs = viewModel.prefs

    val darkMode by prefs.darkMode.collectAsState(initial = false)
    val showGSZ by prefs.showGSZ.collectAsState(initial = false)
    val showAmount by prefs.showAmount.collectAsState(initial = false)
    val showGains by prefs.showGains.collectAsState(initial = false)
    val showCost by prefs.showCost.collectAsState(initial = false)
    val showCostRate by prefs.showCostRate.collectAsState(initial = false)
    val refreshInterval by prefs.refreshInterval.collectAsState(initial = 2)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                Text("主题设置", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(vertical = 12.dp))
            }
            item {
                SettingsSwitch("深色模式", "跟随系统或手动切换", darkMode) {
                    scope.launch { prefs.set("dark_mode", it) }
                }
            }

            item {
                Text("基金列表展示内容", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(vertical = 12.dp))
            }
            item {
                SettingsSwitch("显示估算净值", "在列表中显示基金估算净值", showGSZ) {
                    scope.launch { prefs.set("show_gsz", it) }
                }
            }
            item {
                SettingsSwitch("显示持有金额", "需在编辑中填写份额", showAmount) {
                    scope.launch { prefs.set("show_amount", it) }
                }
            }
            item {
                SettingsSwitch("显示估值收益", "需先开启持有金额", showGains) {
                    scope.launch { prefs.set("show_gains", it) }
                }
            }
            item {
                SettingsSwitch("显示持有收益", "需输入持仓成本价", showCost) {
                    scope.launch { prefs.set("show_cost", it) }
                }
            }
            item {
                SettingsSwitch("显示持有收益率", "需输入持仓成本价", showCostRate) {
                    scope.launch { prefs.set("show_cost_rate", it) }
                }
            }

            item {
                Text("数据刷新", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(vertical = 12.dp))
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("自动刷新间隔", fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(1, 2, 5, 10, 30).forEach { interval ->
                                FilterChip(
                                    selected = refreshInterval == interval,
                                    onClick = { scope.launch { prefs.set("refresh_interval", interval) } },
                                    label = { Text("${interval}分钟") }
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text("数据管理", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(vertical = 12.dp))
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("导入导出配置", fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "可将自选基金配置导出为JSON文本，也可以从文本导入。",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { viewModel.exportConfig() }) {
                                Text("导出配置")
                            }
                            OutlinedButton(onClick = { viewModel.importConfig() }) {
                                Text("导入配置")
                            }
                        }
                    }
                }
            }

            // 关于
            item {
                Text("关于", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(vertical = 12.dp))
            }
            item {
                val year = Calendar.getInstance().get(Calendar.YEAR)
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("基金助手 v1.0.0", fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Copyright © $year F-Droid retain all rights reserved.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "如果您喜欢这个工具，请给作者点个赞吧！😊",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun SettingsSwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium)
            Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
