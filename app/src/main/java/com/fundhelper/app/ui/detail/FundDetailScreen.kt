package com.fundhelper.app.ui.detail

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fundhelper.app.data.model.*
import com.fundhelper.app.ui.theme.DownGreen
import com.fundhelper.app.ui.theme.UpRed
import com.fundhelper.app.util.*

enum class ViewMode { LINE, BAR, LIST }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FundDetailScreen(fundCode: String, onBack: () -> Unit, viewModel: FundDetailViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showPositionDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val tabs = listOf("估值走势", "持仓明细", "历史净值", "累计收益", "基金概况")
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Column { Text(uiState.fund?.name ?: fundCode, style = MaterialTheme.typography.titleMedium); Text(fundCode, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "返回") } },
                actions = {
                    if ((uiState.fund?.shares ?: 0.0) > 0) IconButton(onClick = { showPositionDialog = true }) { Icon(Icons.Default.AccountBalance, "加仓/减仓") }
                    IconButton(onClick = {
                        val csv = viewModel.exportCsv(selectedTab)
                        if (csv.isNotBlank()) {
                            val intent = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, csv) }
                            context.startActivity(Intent.createChooser(intent, "导出CSV"))
                        } else { Toast.makeText(context, "暂无数据", Toast.LENGTH_SHORT).show() }
                    }) { Icon(Icons.Default.Share, "导出CSV") }
                    IconButton(onClick = { viewModel.loadAll() }) { Icon(Icons.Default.Refresh, "刷新") }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            uiState.fundData?.let { data ->
                val rate = data.navChangeRate ?: 0.0; val color = if (rate >= 0) UpRed else DownGreen
                Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                        Column { Text("涨跌幅", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(rate.formatPercent(), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = color) }
                        Column(horizontalAlignment = Alignment.End) { Text("净值", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(data.nav?.toString() ?: data.gsz?.toString() ?: "--", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color); Text(data.pDate?.let { "净值日期: $it" } ?: "净值日期: --", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                }
            }
            ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 8.dp) { tabs.forEachIndexed { i, t -> Tab(selected = selectedTab == i, onClick = { selectedTab = i }, text = { Text(t, fontSize = 12.sp) }) } }
            when (selectedTab) { 0 -> FundTrendTab(uiState); 1 -> FundPositionTab(uiState); 2 -> FundNetDiagramTab(uiState, viewModel); 3 -> FundYieldDiagramTab(uiState, viewModel); 4 -> FundInfoTab(uiState) }
        }
    }
    if (showPositionDialog) PositionDialog(currentShares = uiState.fund?.shares ?: 0.0, onDismiss = { showPositionDialog = false }, onAdd = { viewModel.addPosition(it); showPositionDialog = false }, onReduce = { viewModel.reducePosition(it); showPositionDialog = false })
}

@Composable fun ChartRangeSelector(selectedRange: String, onRangeChange: (String) -> Unit) {
    val ranges = listOf("y" to "月", "3y" to "季", "6y" to "半年", "n" to "一年", "3n" to "三年", "5n" to "五年")
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) { ranges.forEach { (v, l) -> Box(Modifier.clip(RoundedCornerShape(16.dp)).background(if (selectedRange == v) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant).clickable { onRangeChange(v) }.padding(horizontal = 14.dp, vertical = 6.dp), contentAlignment = Alignment.Center) { Text(l, fontSize = 13.sp, color = if (selectedRange == v) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant) } } }
}

@Composable fun ViewModeSelector(mode: ViewMode, onModeChange: (ViewMode) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) { listOf(ViewMode.LINE to "折线图", ViewMode.BAR to "图表", ViewMode.LIST to "列表").forEach { (m, l) -> Text(l, fontSize = 12.sp, color = if (mode == m) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = if (mode == m) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.clickable { onModeChange(m) }.padding(horizontal = 6.dp)) } }
}

@Composable fun FundTrendTab(uiState: FundDetailUiState) {
    val data = uiState.trendData
    if (data.isEmpty()) {
        val netData = uiState.netDiagramData
        if (netData.isNotEmpty()) {
            LazyColumn(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                item { Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Column(Modifier.padding(12.dp)) {
                    Text("非交易时段 — 最近已公布净值", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.height(4.dp))
                    Row(Modifier.fillMaxWidth()) { Text("日期", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); Text("单位净值", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center); Text("涨跌幅", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End) }
                    HorizontalDivider(Modifier.padding(vertical = 4.dp))
                    netData.takeLast(30).forEach { item -> val r = item.changeRate?.replace("%", "")?.toDoubleOrNull() ?: 0.0; val c = if (r >= 0) UpRed else DownGreen; Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(item.date?.takeLast(5) ?: "--", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f)); Text(item.nav?.toString() ?: "--", fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f), textAlign = TextAlign.Center); Text("${item.changeRate ?: "0"}%", fontSize = 12.sp, color = c, modifier = Modifier.weight(1f), textAlign = TextAlign.End) } }
                } } }
            }
        } else {
            val navDate = uiState.fundData?.pDate
            Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("暂无数据", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); if (navDate != null) Text("最近交易日: $navDate", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) } }
        }
        return
    }
    val dwjz = uiState.trendDwjz
    LazyColumn(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        item { Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth()) { Text("时间", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); Text("估算涨跌幅", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center); Text("估算净值", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End) }
            HorizontalDivider(Modifier.padding(vertical = 4.dp))
            data.takeLast(50).forEach { item -> val parts = item.split(","); if (parts.size >= 3) { val cr = parts[2].toDoubleOrNull() ?: 0.0; val en = if (dwjz > 0) dwjz * (1 + cr * 0.01) else 0.0; val c = if (cr >= 0) UpRed else DownGreen; Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(parts[0], fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f)); Text("${String.format("%.2f", cr)}%", fontSize = 12.sp, color = c, modifier = Modifier.weight(1f), textAlign = TextAlign.Center); Text(String.format("%.4f", en), fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f), textAlign = TextAlign.End) } } }
        } } }
    }
}

@Composable fun FundPositionTab(uiState: FundDetailUiState) {
    if (uiState.positions.isEmpty()) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("暂无持仓数据", color = MaterialTheme.colorScheme.onSurfaceVariant) }; return }
    LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        item { uiState.positionDate?.let { Text("截止日期: $it", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.height(4.dp)) }; Row(Modifier.fillMaxWidth()) { Text("股票名称", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f)); Text("涨跌幅", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center); Text("占比", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End) }; HorizontalDivider(Modifier.padding(vertical = 4.dp)) }
        items(uiState.positions) { stock -> val q = uiState.stocks.find { it.code == stock.stockCode }; val cr = q?.changeRate ?: 0.0; val c = if (cr >= 0) UpRed else DownGreen; Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) { Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(2f)) { Text(stock.stockName, fontSize = 13.sp, fontWeight = FontWeight.Medium); Text(stock.stockCode, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }; Text(cr.formatPercent(), fontSize = 13.sp, color = c, modifier = Modifier.weight(1f), textAlign = TextAlign.Center); Text("${String.format("%.2f", stock.ratio)}%", fontSize = 13.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.End) } } }
    }
}

// 计算最大回撤：从最高点到最低点的跌幅，以及从最低点到恢复至前高的天数
fun calcMaxDrawdown(data: List<FundNetDiagramItem>): Triple<Double, String, Int> {
    if (data.size < 2) return Triple(0.0, "", 0)
    var peak = data[0].nav ?: 0.0
    var maxDd = 0.0
    var ddDate = ""
    var troughIdx = -1

    for (i in data.indices) {
        val nav = data[i].nav ?: 0.0
        if (nav > peak) { peak = nav; troughIdx = -1 }
        else {
            val dd = (peak - nav) / peak * 100
            if (dd > maxDd) { maxDd = dd; ddDate = data[i].date ?: ""; troughIdx = i }
        }
    }

    // 从最低点往后数恢复天数（nav >= 前高则恢复）
    var recoveryDays = -1
    if (troughIdx >= 0 && maxDd > 0) {
        val targetPeak = data.subList(0, troughIdx + 1).maxOf { it.nav ?: 0.0 }
        for (i in troughIdx until data.size) {
            if ((data[i].nav ?: 0.0) >= targetPeak) { recoveryDays = i - troughIdx; break }
        }
    }
    return Triple(maxDd, ddDate, recoveryDays)
}

@Composable fun FundNetDiagramTab(uiState: FundDetailUiState, viewModel: FundDetailViewModel) {
    val chartRange by viewModel.chartRange.collectAsStateWithLifecycle(); val data = uiState.netDiagramData; var viewMode by remember { mutableStateOf(ViewMode.LINE) }; var searchDate by remember { mutableStateOf("") }; var newestFirst by remember { mutableStateOf(true) }
    val (maxDd, ddDate, recoveryDays) = remember(data) { calcMaxDrawdown(data) }
    Column { ChartRangeSelector(chartRange, { viewModel.loadChart(it) }); ViewModeSelector(viewMode, { viewMode = it }); if (data.isEmpty()) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("暂无数据", color = MaterialTheme.colorScheme.onSurfaceVariant) }; return }
        if (data.isNotEmpty()) {
            Card(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp), shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = DownGreen.copy(alpha = 0.08f))) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("最大回撤", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Text("${"%.2f".format(maxDd)}%", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DownGreen) }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("发生日期", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(ddDate.ifEmpty { "--" }, fontSize = 13.sp, fontWeight = FontWeight.Medium) }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("修复天数", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(if (recoveryDays > 0) "${recoveryDays}天" else if (recoveryDays == -1) "修复中" else "--", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = UpRed) }
                }
            }
        }
        if (viewMode == ViewMode.LIST) DateSearchBar(searchDate, { searchDate = it }, data.size)
        when (viewMode) {
            ViewMode.LINE -> CanvasLineChart(data.map { it.nav ?: 0.0 }, data.map { it.date ?: "" }, Modifier.fillMaxWidth().padding(12.dp))
            ViewMode.BAR -> SimpleLineChart(data.map { it.nav ?: 0.0 }, data.map { it.date?.takeLast(5) ?: "" }, Modifier.fillMaxWidth().padding(12.dp))
            ViewMode.LIST -> { val raw = if (searchDate.isNotBlank()) data.filter { (it.date ?: "").contains(searchDate) } else data; val f = if (newestFirst) raw.reversed() else raw; LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) { item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Row(Modifier.weight(1f)) { Text("日期", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); Text("单位净值", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center); Text("累计净值", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center); Text("涨跌幅", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End) }; TextButton(onClick = { newestFirst = !newestFirst }) { Text(if (newestFirst) "↓新→旧" else "↑旧→新", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary) } }; HorizontalDivider(Modifier.padding(vertical = 4.dp)) }; items(f) { item -> val r = item.changeRate?.replace("%", "")?.toDoubleOrNull() ?: 0.0; val c = if (r >= 0) UpRed else DownGreen; Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(item.date ?: "--", fontSize = 12.sp, modifier = Modifier.weight(1f)); Text(item.nav?.toString() ?: "--", fontSize = 12.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center); Text(item.totalNav?.toString() ?: "--", fontSize = 12.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center); Text("${item.changeRate ?: "0"}%", fontSize = 12.sp, color = c, modifier = Modifier.weight(1f), textAlign = TextAlign.End) } } } }
        }
    }
}

@Composable fun FundYieldDiagramTab(uiState: FundDetailUiState, viewModel: FundDetailViewModel) {
    val chartRange by viewModel.chartRange.collectAsStateWithLifecycle(); val data = uiState.yieldDiagramData; val indexName = uiState.yieldIndexName; var viewMode by remember { mutableStateOf(ViewMode.LINE) }; var searchDate by remember { mutableStateOf("") }
    Column { ChartRangeSelector(chartRange, { viewModel.loadChart(it) }); ViewModeSelector(viewMode, { viewMode = it }); if (data.isEmpty()) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("暂无数据", color = MaterialTheme.colorScheme.onSurfaceVariant) }; return }; if (viewMode == ViewMode.LIST) DateSearchBar(searchDate, { searchDate = it }, data.size)
        when (viewMode) {
            ViewMode.LINE -> CanvasLineChart(data.map { it.yield ?: 0.0 }, data.map { it.date ?: "" }, Modifier.fillMaxWidth().padding(12.dp))
            ViewMode.BAR -> SimpleLineChart(data.map { it.yield ?: 0.0 }, data.map { it.date?.takeLast(5) ?: "" }, Modifier.fillMaxWidth().padding(12.dp))
            ViewMode.LIST -> { val f = if (searchDate.isNotBlank()) data.filter { (it.date ?: "").contains(searchDate) } else data; LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) { item { Row(Modifier.fillMaxWidth()) { Text("日期", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); Text("涨幅", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center); Text(indexName, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End) }; HorizontalDivider(Modifier.padding(vertical = 4.dp)) }; items(f) { item -> val y = item.yield ?: 0.0; val iy = item.indexYield ?: 0.0; Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(item.date ?: "--", fontSize = 12.sp, modifier = Modifier.weight(1f)); Text("${String.format("%.2f", y)}%", fontSize = 12.sp, color = if (y >= 0) UpRed else DownGreen, modifier = Modifier.weight(1f), textAlign = TextAlign.Center); Text("${String.format("%.2f", iy)}%", fontSize = 12.sp, color = if (iy >= 0) UpRed else DownGreen, modifier = Modifier.weight(1f), textAlign = TextAlign.End) } } } }
        }
    }
}

@Composable fun DateSearchBar(searchDate: String, onSearchChange: (String) -> Unit, dataSize: Int) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Search, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.width(4.dp)); OutlinedTextField(searchDate, onSearchChange, placeholder = { Text("搜索日期 如01-15", fontSize = 11.sp) }, singleLine = true, textStyle = MaterialTheme.typography.bodySmall, modifier = Modifier.width(180.dp).height(48.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)); Spacer(Modifier.width(8.dp)); Text("共${dataSize}条", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
}

@Composable fun CanvasLineChart(data: List<Double>, labels: List<String>, modifier: Modifier = Modifier) { if (data.isEmpty()) return; val minV = data.min(); val maxV = data.max(); val rng = (maxV - minV).coerceAtLeast(0.01); val lc = if (data.last() >= data.first()) UpRed else DownGreen; Card(modifier, shape = RoundedCornerShape(12.dp)) { Column(Modifier.padding(12.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("最高: ${String.format("%.4f", maxV)}", fontSize = 10.sp, color = UpRed); Text("最低: ${String.format("%.4f", minV)}", fontSize = 10.sp, color = DownGreen) }; Spacer(Modifier.height(8.dp)); Canvas(Modifier.fillMaxWidth().height(220.dp)) { if (data.size < 2) return@Canvas; val w = size.width; val h = size.height; val pad = 16f; val dW = w - pad * 2; val dH = h - pad * 2; val sX = dW / (data.size - 1); val fp = Path(); data.forEachIndexed { i, v -> val x = pad + i * sX; val y = pad + dH * (1 - ((v - minV) / rng)).toFloat(); if (i == 0) { fp.moveTo(x, pad + dH); fp.lineTo(x, y) } else fp.lineTo(x, y) }; fp.lineTo(pad + (data.size - 1) * sX, pad + dH); fp.close(); drawPath(fp, lc.copy(alpha = 0.08f)); val lp = Path(); data.forEachIndexed { i, v -> val x = pad + i * sX; val y = pad + dH * (1 - ((v - minV) / rng)).toFloat(); if (i == 0) lp.moveTo(x, y) else lp.lineTo(x, y) }; drawPath(lp, lc, style = Stroke(2.5f)); if (data.isNotEmpty()) { val lx = pad + (data.size - 1) * sX; val ly = pad + dH * (1 - ((data.last() - minV) / rng)).toFloat(); drawCircle(lc, 5f, Offset(lx, ly)) } }; Spacer(Modifier.height(4.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { val sc = minOf(6, labels.size); val st = if (sc > 1) (labels.size - 1) / (sc - 1) else 0; for (i in 0 until sc) { val idx = (i * st).coerceAtMost(labels.size - 1); Text(labels[idx].takeLast(5), fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) } } } } }

@Composable fun SimpleLineChart(data: List<Double>, labels: List<String>, modifier: Modifier = Modifier) { if (data.isEmpty()) return; val minV = data.min(); val maxV = data.max(); val rng = (maxV - minV).coerceAtLeast(0.01); val lc = if (data.last() >= data.first()) UpRed else DownGreen; Card(modifier, shape = RoundedCornerShape(12.dp)) { Column(Modifier.padding(12.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("最高: ${String.format("%.4f", maxV)}", fontSize = 10.sp, color = UpRed); Text("最低: ${String.format("%.4f", minV)}", fontSize = 10.sp, color = DownGreen) }; Spacer(Modifier.height(8.dp)); Canvas(Modifier.fillMaxWidth().height(220.dp)) { if (data.size < 2) return@Canvas; val w = size.width; val h = size.height; val pad = 20f; val dW = w - pad * 2; val dH = h - pad * 2; val bw = (dW / data.size) * 0.7f; val gap = (dW / data.size) * 0.3f; data.forEachIndexed { i, v -> val x = pad + i * (bw + gap); val bh = (dH * ((v - minV) / rng)).toFloat(); val y = pad + dH - bh; drawRect(if (v >= 0) UpRed else DownGreen, Offset(x, y), androidx.compose.ui.geometry.Size(bw, bh.coerceAtLeast(2f))) } }; Spacer(Modifier.height(4.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { val sc = minOf(6, labels.size); val st = if (sc > 1) (labels.size - 1) / (sc - 1) else 0; for (i in 0 until sc) { val idx = (i * st).coerceAtMost(labels.size - 1); Text(labels[idx], fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) } } } } }

@Composable fun FundInfoTab(uiState: FundDetailUiState) {
    val info = uiState.fundInfo; if (info == null) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("暂无数据", color = MaterialTheme.colorScheme.onSurfaceVariant) }; return }
    LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        item { Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Column(Modifier.padding(12.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("基金名称", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(info.name ?: "--", fontSize = 13.sp, fontWeight = FontWeight.Medium) }; Spacer(Modifier.height(4.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("基金代码", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(info.code ?: "--", fontSize = 13.sp, fontWeight = FontWeight.Medium) }; Spacer(Modifier.height(4.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("基金类型", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(info.type ?: "--", fontSize = 13.sp, fontWeight = FontWeight.Medium) }; Spacer(Modifier.height(4.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("基金公司", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(info.company ?: "--", fontSize = 13.sp, fontWeight = FontWeight.Medium) }; Spacer(Modifier.height(4.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("基金经理", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(info.manager ?: "--", fontSize = 13.sp, fontWeight = FontWeight.Medium) }; Spacer(Modifier.height(4.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("成立日期", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(info.navDate ?: "--", fontSize = 13.sp, fontWeight = FontWeight.Medium) }; Spacer(Modifier.height(4.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("基金规模", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(info.scale?.let { "${it / 100000000}亿" } ?: "--", fontSize = 13.sp, fontWeight = FontWeight.Medium) } } } }
        item { Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Column(Modifier.padding(12.dp)) { Text("历史业绩", fontSize = 14.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(6.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text("近1月", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Row(verticalAlignment = Alignment.CenterVertically) { Text(info.return1M?.let { "${String.format("%.2f", it)}%" } ?: "--", fontSize = 13.sp, color = if ((info.return1M ?: 0.0) >= 0) UpRed else DownGreen); if (info.rank1M != null) Text(" | ${info.rank1M}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) } }; Spacer(Modifier.height(4.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text("近3月", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Row(verticalAlignment = Alignment.CenterVertically) { Text(info.return3M?.let { "${String.format("%.2f", it)}%" } ?: "--", fontSize = 13.sp, color = if ((info.return3M ?: 0.0) >= 0) UpRed else DownGreen); if (info.rank3M != null) Text(" | ${info.rank3M}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) } }; Spacer(Modifier.height(4.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text("近6月", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Row(verticalAlignment = Alignment.CenterVertically) { Text(info.return6M?.let { "${String.format("%.2f", it)}%" } ?: "--", fontSize = 13.sp, color = if ((info.return6M ?: 0.0) >= 0) UpRed else DownGreen); if (info.rank6M != null) Text(" | ${info.rank6M}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) } }; Spacer(Modifier.height(4.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text("近1年", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Row(verticalAlignment = Alignment.CenterVertically) { Text(info.return1Y?.let { "${String.format("%.2f", it)}%" } ?: "--", fontSize = 13.sp, color = if ((info.return1Y ?: 0.0) >= 0) UpRed else DownGreen); if (info.rank1Y != null) Text(" | ${info.rank1Y}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) } } } } }
    }
}

@Composable fun PositionDialog(currentShares: Double, onDismiss: () -> Unit, onAdd: (Double) -> Unit, onReduce: (Double) -> Unit) {
    var addShares by remember { mutableStateOf("") }; var reduceShares by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("调整仓位  当前: ${currentShares.toString().removeSuffix(".0")}份") }, text = { Column { OutlinedTextField(addShares, { addShares = it }, label = { Text("买入份额") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)); Spacer(Modifier.height(8.dp)); Button({ addShares.toDoubleOrNull()?.let { onAdd(it) } }, Modifier.fillMaxWidth()) { Text("确认加仓") }; Spacer(Modifier.height(12.dp)); OutlinedTextField(reduceShares, { reduceShares = it }, label = { Text("卖出份额") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)); Spacer(Modifier.height(8.dp)); Button({ reduceShares.toDoubleOrNull()?.let { onReduce(it) } }, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = DownGreen)) { Text("确认减仓") } } }, confirmButton = {}, dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } })
}
