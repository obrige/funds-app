package com.fundhelper.app.ui.market

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fundhelper.app.data.model.FlowItem
import com.fundhelper.app.data.model.SectorItem
import com.fundhelper.app.ui.theme.DownGreen
import com.fundhelper.app.ui.theme.UpRed
import com.fundhelper.app.util.formatAmount
import com.fundhelper.app.util.formatPercent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketCenterScreen(
    onBack: () -> Unit,
    viewModel: MarketViewModel = hiltViewModel()
) {
    val sectors by viewModel.sectors.collectAsStateWithLifecycle()
    val northFlow by viewModel.northFlow.collectAsStateWithLifecycle()
    val southFlow by viewModel.southFlow.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) { viewModel.loadAll() }

    val tabs = listOf("行业板块", "北向资金", "南向资金")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("行情中心") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "返回") } },
                actions = { IconButton(onClick = { viewModel.loadAll() }) { Icon(Icons.Default.Refresh, "刷新") } }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) })
                }
            }
            when (selectedTab) {
                0 -> SectorTab(sectors, isLoading)
                1 -> FlowTab(northFlow, isLoading, "北向资金", "沪股通", "深股通")
                2 -> FlowTab(southFlow, isLoading, "南向资金", "沪港通", "深港股")
            }
        }
    }
}

@Composable
fun SectorTab(sectors: List<SectorItem>, isLoading: Boolean) {
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        item {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("板块名称", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f))
                Text("涨跌幅", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Text("主力净流入", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        }
        items(sectors) { sector ->
            val color = if ((sector.changeRate ?: 0.0) >= 0) UpRed else DownGreen
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(sector.name ?: "--", fontSize = 13.sp, modifier = Modifier.weight(2f), maxLines = 1)
                    Text(sector.changeRate?.formatPercent() ?: "--", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = color, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    Text(sector.mainNetInflow?.formatAmount() ?: "--", fontSize = 12.sp, color = if ((sector.mainNetInflow ?: 0.0) >= 0) UpRed else DownGreen, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
                }
            }
        }
    }
}

@Composable
fun FlowTab(flows: List<FlowItem>, isLoading: Boolean, title: String, name1: String, name2: String) {
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        item {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("时间", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("合计(亿)", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Text("$name1(亿)", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Text("$name2(亿)", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        }
        items(flows.takeLast(20).reversed()) { flow ->
            val total = (flow.totalFlow ?: 0.0) / 10000
            val sh = (flow.shFlow ?: 0.0) / 10000
            val sz = (flow.szFlow ?: 0.0) / 10000
            val color = if (total >= 0) UpRed else DownGreen
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text(flow.time?.let { java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(it * 1000)) } ?: "--", fontSize = 12.sp, modifier = Modifier.weight(1f))
                Text(String.format("%.2f", total), fontSize = 12.sp, fontWeight = FontWeight.Medium, color = color, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Text(String.format("%.2f", sh), fontSize = 12.sp, color = if (sh >= 0) UpRed else DownGreen, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Text(String.format("%.2f", sz), fontSize = 12.sp, color = if (sz >= 0) UpRed else DownGreen, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
            }
        }
    }
}
