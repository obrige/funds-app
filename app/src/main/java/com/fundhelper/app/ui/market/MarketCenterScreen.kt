package com.fundhelper.app.ui.market

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
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
import com.fundhelper.app.data.model.SectorItem
import com.fundhelper.app.ui.theme.DownGreen
import com.fundhelper.app.ui.theme.UpRed
import com.fundhelper.app.util.formatAmount
import com.fundhelper.app.util.formatPercent

private data class FlowParsed(val time: String, val main: Double, val small: Double, val mid: Double, val big: Double, val superVal: Double)
private fun parseFlowCsv(lines: List<String>) = lines.mapNotNull { line ->
    val a = line.split(","); if (a.size < 6) null else FlowParsed(a[0], a[1].toDoubleOrNull() ?: 0.0, a[2].toDoubleOrNull() ?: 0.0, a[3].toDoubleOrNull() ?: 0.0, a[4].toDoubleOrNull() ?: 0.0, a[5].toDoubleOrNull() ?: 0.0)
}

private data class NsParsed(val time: String, val sh: Double, val sz: Double, val total: Double)
private fun parseNsCsv(lines: List<String>) = lines.mapNotNull { line ->
    val a = line.split(","); if (a.size < 6) null else NsParsed(a[0], a[1].toDoubleOrNull() ?: 0.0, a[3].toDoubleOrNull() ?: 0.0, a[5].toDoubleOrNull() ?: 0.0)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketCenterScreen(onBack: () -> Unit, viewModel: MarketViewModel = hiltViewModel()) {
    val sectors by viewModel.sectors.collectAsStateWithLifecycle()
    val marketFlowRaw by viewModel.marketFlow.collectAsStateWithLifecycle()
    val northFlowRaw by viewModel.northFlow.collectAsStateWithLifecycle()
    val southFlowRaw by viewModel.southFlow.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { viewModel.loadAll() }
    Scaffold(
        topBar = { TopAppBar(title = { Text("行情中心") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "返回") } }, actions = { IconButton(onClick = { viewModel.loadAll() }) { Icon(Icons.Default.Refresh, "刷新") } }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 8.dp) {
                listOf("大盘资金", "行业板块", "北向资金", "南向资金").forEachIndexed { i, t -> Tab(selected = selectedTab == i, onClick = { selectedTab = i }, text = { Text(t, fontSize = 13.sp) }) }
            }
            when (selectedTab) {
                0 -> FundFlowTab(marketFlowRaw, isLoading)
                1 -> SectorTab(sectors, isLoading)
                2 -> NsFlowTab(northFlowRaw, isLoading, "北向资金", "沪股通", "深股通")
                3 -> NsFlowTab(southFlowRaw, isLoading, "南向资金", "港股通(沪)", "港股通(深)")
            }
        }
    }
}

@Composable
fun FundFlowTab(raw: List<String>, isLoading: Boolean) {
    if (isLoading) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }; return }
    val flows = remember(raw) { parseFlowCsv(raw) }
    if (flows.isEmpty()) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("暂无数据", color = MaterialTheme.colorScheme.onSurfaceVariant) }; return }
    val mainD = flows.map { it.main / 1_0000_0000 }; val supD = flows.map { it.superVal / 1_0000_0000 }; val bigD = flows.map { it.big / 1_0000_0000 }; val midD = flows.map { it.mid / 1_0000_0000 }; val smD = flows.map { it.small / 1_0000_0000 }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        flows.lastOrNull()?.let { last ->
            Card(Modifier.fillMaxWidth().padding(12.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                Column(Modifier.padding(12.dp)) {
                    Text("资金流向概览", fontWeight = FontWeight.Bold, fontSize = 14.sp); Spacer(Modifier.height(8.dp))
                    SRow("主力净流入", last.main / 1_0000_0000); SRow("超大单净流入", last.superVal / 1_0000_0000); SRow("大单净流入", last.big / 1_0000_0000); SRow("中单净流入", last.mid / 1_0000_0000); SRow("小单净流入", last.small / 1_0000_0000)
                }
            }
        }
        Text("分时资金流向（亿元）", fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        MLineChart(listOf("主力" to mainD to UpRed, "超大单" to supD to Color(0xFFFF6B35), "大单" to bigD to Color(0xFFFF9800), "中单" to midD to Color(0xFF2196F3), "小单" to smD to DownGreen), Modifier.fillMaxWidth().padding(12.dp).height(240.dp))
        Text("明细（最近20条）", fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        flows.takeLast(20).reversed().forEach { f ->
            val m = f.main / 1_0000_0000
            Card(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 2.dp), shape = RoundedCornerShape(6.dp)) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(f.time.takeLast(8), fontSize = 11.sp, modifier = Modifier.weight(1f))
                    Text(String.format("%.2f亿", m), fontSize = 11.sp, fontWeight = FontWeight.Medium, color = if (m >= 0) UpRed else DownGreen)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable fun SRow(label: String, value: Double) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(String.format("%.2f 亿", value), fontSize = 12.sp, fontWeight = FontWeight.Medium, color = if (value >= 0) UpRed else DownGreen)
    }
}

// ==================== 行业板块 ====================
@Composable
fun SectorTab(sectors: List<SectorItem>, isLoading: Boolean) {
    if (isLoading) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }; return }
    if (sectors.isEmpty()) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("暂无数据", color = MaterialTheme.colorScheme.onSurfaceVariant) }; return }

    val barData = sectors.map { (it.mainNetInflow ?: 0.0) / 1_0000_0000 }
    val barLabels = sectors.map { it.name ?: "" }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("主力净流入排行（亿元）全部${sectors.size}个板块", fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        HBarChart(data = barData, labels = barLabels, modifier = Modifier.fillMaxWidth().padding(12.dp))
        Spacer(Modifier.height(16.dp))
    }
}

// ==================== 北向/南向 ====================
@Composable
fun NsFlowTab(raw: List<String>, isLoading: Boolean, title: String, n1: String, n2: String) {
    if (isLoading) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }; return }
    val flows = remember(raw) { parseNsCsv(raw) }
    if (flows.isEmpty()) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("暂无数据", color = MaterialTheme.colorScheme.onSurfaceVariant) }; return }
    val last = flows.lastOrNull { it.total != 0.0 } ?: flows.lastOrNull() ?: return
    val tL = flows.map { it.total / 10000 }; val sL = flows.map { it.sh / 10000 }; val zL = flows.map { it.sz / 10000 }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Card(Modifier.fillMaxWidth().padding(12.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
            Column(Modifier.padding(12.dp)) {
                Text("$title 当日汇总", fontWeight = FontWeight.Bold, fontSize = 14.sp); Spacer(Modifier.height(8.dp))
                SRow("$n1 净流入", last.sh / 10000); SRow("$n2 净流入", last.sz / 10000); SRow("${title}合计", last.total / 10000)
            }
        }
        Text("$title 分时走势（亿元）", fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        MLineChart(listOf(title to tL to UpRed, n1 to sL to Color(0xFFFF9800), n2 to zL to Color(0xFF2196F3)), Modifier.fillMaxWidth().padding(12.dp).height(200.dp))
        Text("明细（最近20条）", fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        flows.takeLast(20).reversed().forEach { f ->
            val t = f.total / 10000; val sh = f.sh / 10000; val sz = f.sz / 10000
            Card(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 2.dp), shape = RoundedCornerShape(6.dp)) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(f.time.takeLast(8), fontSize = 11.sp, modifier = Modifier.weight(1f))
                    Text(String.format("%.2f", t), fontSize = 11.sp, fontWeight = FontWeight.Medium, color = if (t >= 0) UpRed else DownGreen, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    Text(String.format("%.2f", sh), fontSize = 11.sp, color = if (sh >= 0) UpRed else DownGreen, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    Text(String.format("%.2f", sz), fontSize = 11.sp, color = if (sz >= 0) UpRed else DownGreen, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

// ==================== 多线折线图 ====================
@Composable
fun MLineChart(lines: List<Pair<Pair<String, List<Double>>, Color>>, modifier: Modifier = Modifier) {
    if (lines.isEmpty() || lines.any { it.first.second.isEmpty() }) { Box(modifier, contentAlignment = Alignment.Center) { Text("暂无图表数据", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }; return }
    val all = lines.flatMap { it.first.second }; val minV = all.min(); val maxV = all.max(); val rng = (maxV - minV).coerceAtLeast(0.01)
    Card(modifier, shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(8.dp)) {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                lines.forEach { (p, c) -> Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(c)); Spacer(Modifier.width(4.dp)); Text(p.first, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
            }
            Spacer(Modifier.height(4.dp))
            Canvas(Modifier.fillMaxWidth().weight(1f)) {
                val w = size.width; val h = size.height; val pad = 8f; val dW = w - pad * 2; val dH = h - pad * 2
                if (minV < 0 && maxV > 0) { val zY = pad + dH * (1 - ((0f - minV) / rng)).toFloat(); drawLine(Color.Gray.copy(alpha = 0.3f), Offset(pad, zY), Offset(pad + dW, zY), strokeWidth = 1f) }
                lines.forEach { (p, c) -> val d = p.second; if (d.size >= 2) { val sX = dW / (d.size - 1); val path = Path(); d.forEachIndexed { i, v -> val x = pad + i * sX; val y = pad + dH * (1 - ((v.toFloat() - minV) / rng)).toFloat(); if (i == 0) path.moveTo(x, y) else path.lineTo(x, y) }; drawPath(path, c, style = Stroke(2f)) } }
            }
        }
    }
}

// ==================== 横向柱状图：零线居中，正值右红，负值左绿 ====================
@Composable
fun HBarChart(data: List<Double>, labels: List<String>, modifier: Modifier = Modifier) {
    if (data.isEmpty()) return
    val maxAbs = maxOf(data.maxOrNull() ?: 0.0, kotlin.math.abs(data.minOrNull() ?: 0.0)).coerceAtLeast(0.01)
    Card(modifier, shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(8.dp).verticalScroll(rememberScrollState())) {
            data.forEachIndexed { i, v ->
                val c = if (v >= 0) UpRed else DownGreen
                val label = labels.getOrElse(i) { "" }
                val frac = (kotlin.math.abs(v) / maxAbs).toFloat().coerceAtMost(1f)
                Row(Modifier.fillMaxWidth().padding(vertical = 1.dp).height(22.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(label, fontSize = 9.sp, modifier = Modifier.width(56.dp), maxLines = 1)
                    // 零线居中：左半=负值区(绿)，右半=正值区(红)
                    Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                        // 零线
                        Box(Modifier.fillMaxHeight().width(1.dp).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)))
                        // 正/负值柱
                        Box(Modifier.fillMaxHeight().fillMaxWidth(frac / 2).align(if (v >= 0) Alignment.CenterStart else Alignment.CenterEnd).clip(RoundedCornerShape(3.dp)).background(c.copy(alpha = 0.7f)))
                    }
                    Text(String.format("%.2f", v), fontSize = 9.sp, modifier = Modifier.width(52.dp), textAlign = TextAlign.End, color = c)
                }
            }
        }
    }
}
