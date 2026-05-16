package com.fundhelper.app.ui.market

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fundhelper.app.data.model.FlowItem
import com.fundhelper.app.data.model.FundFlowItem
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
    val marketFlow by viewModel.marketFlow.collectAsStateWithLifecycle()
    val northFlow by viewModel.northFlow.collectAsStateWithLifecycle()
    val southFlow by viewModel.southFlow.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) { viewModel.loadAll() }

    val tabs = listOf("大盘资金", "行业板块", "北向资金", "南向资金")

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
            ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 8.dp) {
                tabs.forEachIndexed { index, title ->
                    Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title, fontSize = 13.sp) })
                }
            }
            when (selectedTab) {
                0 -> MarketFundFlowTab(marketFlow, isLoading)
                1 -> SectorTab(sectors, isLoading)
                2 -> FlowTab(northFlow, isLoading, "北向资金", "沪股通", "深股通")
                3 -> FlowTab(southFlow, isLoading, "南向资金", "港股通(沪)", "港股通(深)")
            }
        }
    }
}

// ==================== Tab 0: 大盘资金流向 ====================
@Composable
fun MarketFundFlowTab(flows: List<FundFlowItem>, isLoading: Boolean) {
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    if (flows.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("暂无数据", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        return
    }

    val mainData = flows.map { (it.mainInflow ?: 0.0) / 1_0000_0000 }
    val superData = flows.map { (it.superInflow ?: 0.0) / 1_0000_0000 }
    val bigData = flows.map { (it.bigInflow ?: 0.0) / 1_0000_0000 }
    val midData = flows.map { (it.midInflow ?: 0.0) / 1_0000_0000 }
    val smallData = flows.map { (it.smallInflow ?: 0.0) / 1_0000_0000 }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        val lastFlow = flows.lastOrNull()
        if (lastFlow != null) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("资金流向概览", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowSummaryRow("主力净流入", (lastFlow.mainInflow ?: 0.0) / 1_0000_0000)
                    FlowSummaryRow("超大单净流入", (lastFlow.superInflow ?: 0.0) / 1_0000_0000)
                    FlowSummaryRow("大单净流入", (lastFlow.bigInflow ?: 0.0) / 1_0000_0000)
                    FlowSummaryRow("中单净流入", (lastFlow.midInflow ?: 0.0) / 1_0000_0000)
                    FlowSummaryRow("小单净流入", (lastFlow.smallInflow ?: 0.0) / 1_0000_0000)
                }
            }
        }

        Text("分时资金流向（亿元）", fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        MultiLineChart(
            lines = listOf(
                "主力" to mainData to UpRed,
                "超大单" to superData to Color(0xFFFF6B35),
                "大单" to bigData to Color(0xFFFF9800),
                "中单" to midData to Color(0xFF2196F3),
                "小单" to smallData to DownGreen
            ),
            modifier = Modifier.fillMaxWidth().padding(12.dp).height(240.dp)
        )

        Text("明细（最近20条）", fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        flows.takeLast(20).reversed().forEach { flow ->
            val main = (flow.mainInflow ?: 0.0) / 1_0000_0000
            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 2.dp), shape = RoundedCornerShape(6.dp)) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(flow.timestamp?.let { formatFlowTime(it) } ?: "--", fontSize = 11.sp, modifier = Modifier.weight(1f))
                    Text(String.format("%.2f亿", main), fontSize = 11.sp, fontWeight = FontWeight.Medium, color = if (main >= 0) UpRed else DownGreen)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun FlowSummaryRow(label: String, value: Double) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(String.format("%.2f 亿", value), fontSize = 12.sp, fontWeight = FontWeight.Medium, color = if (value >= 0) UpRed else DownGreen)
    }
}

private fun formatFlowTime(timestamp: Long): String {
    val date = java.util.Date(timestamp * 1000)
    val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
    return sdf.format(date)
}

// ==================== Tab 1: 行业板块 ====================
@Composable
fun SectorTab(sectors: List<SectorItem>, isLoading: Boolean) {
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    if (sectors.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("暂无数据", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        return
    }

    val topSectors = sectors.take(30)
    val barData = topSectors.map { (it.mainNetInflow ?: 0.0) / 1_0000_0000 }
    val barLabels = topSectors.map { it.name ?: "" }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("主力净流入排行（亿元）", fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        HorizontalBarChart(
            data = barData,
            labels = barLabels,
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text("全部板块（${sectors.size}）", fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
}

// ==================== Tab 2/3: 北向/南向资金 ====================
@Composable
fun FlowTab(flows: List<FlowItem>, isLoading: Boolean, title: String, name1: String, name2: String) {
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    if (flows.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("暂无数据", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        return
    }

    val lastValid = flows.lastOrNull { it.totalFlow != null && it.totalFlow != 0.0 } ?: flows.lastOrNull()
    val totalFlow = (lastValid?.totalFlow ?: 0.0) / 10000
    val shFlow = (lastValid?.shFlow ?: 0.0) / 10000
    val szFlow = (lastValid?.szFlow ?: 0.0) / 10000

    val chartData = flows.takeLast(120)
    val totalLine = chartData.map { (it.totalFlow ?: 0.0) / 10000 }
    val shLine = chartData.map { (it.shFlow ?: 0.0) / 10000 }
    val szLine = chartData.map { (it.szFlow ?: 0.0) / 10000 }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("$title 当日汇总", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                FlowSummaryRow("$name1 净流入", shFlow)
                FlowSummaryRow("$name2 净流入", szFlow)
                FlowSummaryRow("${title}合计", totalFlow)
            }
        }

        Text("$title 分时走势（亿元）", fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        MultiLineChart(
            lines = listOf(
                title to totalLine to UpRed,
                name1 to shLine to Color(0xFFFF9800),
                name2 to szLine to Color(0xFF2196F3)
            ),
            modifier = Modifier.fillMaxWidth().padding(12.dp).height(200.dp)
        )

        Text("明细（最近20条）", fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        flows.takeLast(20).reversed().forEach { flow ->
            val total = (flow.totalFlow ?: 0.0) / 10000
            val sh = (flow.shFlow ?: 0.0) / 10000
            val sz = (flow.szFlow ?: 0.0) / 10000
            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 2.dp), shape = RoundedCornerShape(6.dp)) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(flow.time?.let { formatFlowTime(it) } ?: "--", fontSize = 11.sp, modifier = Modifier.weight(1f))
                    Text(String.format("%.2f", total), fontSize = 11.sp, fontWeight = FontWeight.Medium, color = if (total >= 0) UpRed else DownGreen, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    Text(String.format("%.2f", sh), fontSize = 11.sp, color = if (sh >= 0) UpRed else DownGreen, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    Text(String.format("%.2f", sz), fontSize = 11.sp, color = if (sz >= 0) UpRed else DownGreen, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ==================== 通用图表组件 ====================

@Composable
fun MultiLineChart(
    lines: List<Pair<Pair<String, List<Double>>, Color>>,
    modifier: Modifier = Modifier
) {
    if (lines.isEmpty() || lines.any { it.first.second.isEmpty() }) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) { Text("暂无图表数据", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        return
    }

    val allValues = lines.flatMap { it.first.second }
    val minVal = allValues.min()
    val maxVal = allValues.max()
    val range = (maxVal - minVal).coerceAtLeast(0.01)

    Card(modifier = modifier, shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                lines.forEach { (pair, color) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(color))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(pair.first, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
                val w = size.width
                val h = size.height
                val pad = 8f
                val drawW = w - pad * 2
                val drawH = h - pad * 2

                if (minVal < 0 && maxVal > 0) {
                    val zeroY = pad + drawH * (1 - ((0f - minVal) / range)).toFloat()
                    drawLine(Color.Gray.copy(alpha = 0.3f), Offset(pad, zeroY), Offset(pad + drawW, zeroY), strokeWidth = 1f)
                }

                lines.forEach { (pair, color) ->
                    val data = pair.second
                    if (data.size < 2) return@forEach
                    val stepX = drawW / (data.size - 1)
                    val path = Path()
                    data.forEachIndexed { i, v ->
                        val x = pad + i * stepX
                        val y = pad + drawH * (1 - ((v.toFloat() - minVal) / range)).toFloat()
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(path, color = color, style = Stroke(width = 2f))
                }
            }
        }
    }
}

@Composable
fun HorizontalBarChart(
    data: List<Double>,
    labels: List<String>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return
    val maxAbs = maxOf(data.maxOrNull() ?: 0.0, kotlin.math.abs(data.minOrNull() ?: 0.0)).coerceAtLeast(0.01)

    Card(modifier = modifier, shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            data.forEachIndexed { index, value ->
                val barColor = if (value >= 0) UpRed else DownGreen
                val label = if (index < labels.size) labels[index] else ""
                val fraction = (kotlin.math.abs(value) / maxAbs).toFloat().coerceAtMost(1f)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(label, fontSize = 10.sp, modifier = Modifier.width(64.dp), maxLines = 1)
                    Box(modifier = Modifier.weight(1f).height(16.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fraction)
                                .clip(RoundedCornerShape(3.dp))
                                .background(barColor.copy(alpha = 0.7f))
                        )
                    }
                    Text(
                        String.format("%.2f", value),
                        fontSize = 10.sp,
                        modifier = Modifier.width(56.dp),
                        textAlign = TextAlign.End,
                        color = barColor
                    )
                }
            }
        }
    }
}
