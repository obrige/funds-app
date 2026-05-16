package com.fundhelper.app.ui.detail

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
fun FundDetailScreen(
    fundCode: String,
    onBack: () -> Unit,
    viewModel: FundDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showPositionDialog by remember { mutableStateOf(false) }
    val tabs = listOf("估值走势", "持仓明细", "历史净值", "累计收益", "基金概况")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(uiState.fund?.name ?: fundCode, style = MaterialTheme.typography.titleMedium)
                        Text(fundCode, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "返回") } },
                actions = {
                    if ((uiState.fund?.shares ?: 0.0) > 0) {
                        IconButton(onClick = { showPositionDialog = true }) { Icon(Icons.Default.AccountBalance, "加仓/减仓") }
                    }
                    IconButton(onClick = { viewModel.loadAll() }) { Icon(Icons.Default.Refresh, "刷新") }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            uiState.fundData?.let { data ->
                val rate = data.gszzl ?: 0.0
                val color = if (rate >= 0) UpRed else DownGreen
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text("估算涨跌幅", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(rate.formatPercent(), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = color)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("估算净值", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(data.gsz?.toString() ?: "--", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
                            Text("更新: ${data.gzTime?.takeLast(8) ?: "--"}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 8.dp) {
                tabs.forEachIndexed { index, title ->
                    Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title, fontSize = 12.sp) })
                }
            }
            when (selectedTab) {
                0 -> FundTrendTab(uiState)
                1 -> FundPositionTab(uiState)
                2 -> FundNetDiagramTab(uiState, viewModel)
                3 -> FundYieldDiagramTab(uiState, viewModel)
                4 -> FundInfoTab(uiState)
            }
        }
    }
    if (showPositionDialog) {
        PositionDialog(
            currentShares = uiState.fund?.shares ?: 0.0,
            onDismiss = { showPositionDialog = false },
            onAdd = { viewModel.addPosition(it); showPositionDialog = false },
            onReduce = { viewModel.reducePosition(it); showPositionDialog = false }
        )
    }
}

@Composable
fun ChartRangeSelector(selectedRange: String, onRangeChange: (String) -> Unit) {
    val ranges = listOf("y" to "月", "3y" to "季", "6y" to "半年", "n" to "一年", "3n" to "三年", "5n" to "五年")
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        ranges.forEach { (value, label) ->
            Box(
                modifier = Modifier.clip(RoundedCornerShape(16.dp))
                    .background(if (selectedRange == value) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onRangeChange(value) }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(label, fontSize = 13.sp, color = if (selectedRange == value) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun ViewModeSelector(mode: ViewMode, onModeChange: (ViewMode) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val items = listOf(ViewMode.LINE to "折线图", ViewMode.BAR to "图表", ViewMode.LIST to "列表")
        items.forEach { (m, label) ->
            Text(
                label,
                fontSize = 12.sp,
                color = if (mode == m) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (mode == m) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.clickable { onModeChange(m) }.padding(horizontal = 6.dp)
            )
        }
    }
}

// Tab 0: 估值走势
@Composable
fun FundTrendTab(uiState: FundDetailUiState) {
    val trendData = uiState.trendData
    val dwjz = uiState.trendDwjz
    if (trendData.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("暂无走势数据", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        return
    }
    LazyColumn(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        item {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("时间", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("估算涨跌幅", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        Text("估算净值", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    trendData.takeLast(50).forEach { item ->
                        val parts = item.split(",")
                        if (parts.size >= 3) {
                            val changeRate = parts[2].toDoubleOrNull() ?: 0.0
                            val estimatedNav = if (dwjz > 0) dwjz * (1 + changeRate * 0.01) else 0.0
                            val color = if (changeRate >= 0) UpRed else DownGreen
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(parts[0], fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                                Text("${String.format("%.2f", changeRate)}%", fontSize = 12.sp, color = color, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                                Text(String.format("%.4f", estimatedNav), fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Tab 1: 持仓明细
@Composable
fun FundPositionTab(uiState: FundDetailUiState) {
    if (uiState.positions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("暂无持仓数据", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        return
    }
    LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        item {
            uiState.positionDate?.let { Text("截止日期: $it", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(modifier = Modifier.height(4.dp)) }
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("股票名称", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f))
                Text("涨跌幅", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Text("占比", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        }
        items(uiState.positions) { stock ->
            val quote = uiState.stocks.find { it.code == stock.stockCode }
            val changeRate = quote?.changeRate ?: 0.0
            val color = if (changeRate >= 0) UpRed else DownGreen
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(2f)) {
                        Text(stock.stockName, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Text(stock.stockCode, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(changeRate.formatPercent(), fontSize = 13.sp, color = color, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    Text("${String.format("%.2f", stock.ratio)}%", fontSize = 13.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                }
            }
        }
    }
}

// Tab 2: 历史净值（折线图/图表/列表三模式）
@Composable
fun FundNetDiagramTab(uiState: FundDetailUiState, viewModel: FundDetailViewModel) {
    val chartRange by viewModel.chartRange.collectAsStateWithLifecycle()
    val data = uiState.netDiagramData
    var viewMode by remember { mutableStateOf(ViewMode.LINE) }
    var searchDate by remember { mutableStateOf("") }

    Column {
        ChartRangeSelector(selectedRange = chartRange, onRangeChange = { viewModel.loadChart(it) })
        ViewModeSelector(mode = viewMode, onModeChange = { viewMode = it })

        if (data.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("暂无数据", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            return
        }

        // 日期搜索
        DateSearchBar(searchDate = searchDate, onSearchChange = { searchDate = it }, dataSize = data.size)

        when (viewMode) {
            ViewMode.LINE -> CanvasLineChart(
                data = data.map { it.nav ?: 0.0 },
                labels = data.map { it.date ?: "" },
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            )
            ViewMode.BAR -> SimpleLineChart(
                data = data.map { it.nav ?: 0.0 },
                labels = data.map { it.date?.takeLast(5) ?: "" },
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            )
            ViewMode.LIST -> {
                val filtered = if (searchDate.isNotBlank()) {
                    data.filter { (it.date ?: "").contains(searchDate) }
                } else data
                LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    item {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text("日期", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text("单位净值", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            Text("累计净值", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            Text("涨跌幅", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    }
                    items(filtered) { item ->
                        val rate = item.changeRate?.replace("%", "")?.toDoubleOrNull() ?: 0.0
                        val color = if (rate >= 0) UpRed else DownGreen
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(item.date ?: "--", fontSize = 12.sp, modifier = Modifier.weight(1f))
                            Text(item.nav?.toString() ?: "--", fontSize = 12.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            Text(item.totalNav?.toString() ?: "--", fontSize = 12.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            Text("${item.changeRate ?: "0"}%", fontSize = 12.sp, color = color, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                        }
                    }
                }
            }
        }
    }
}

// Tab 3: 累计收益（折线图/图表/列表三模式）
@Composable
fun FundYieldDiagramTab(uiState: FundDetailUiState, viewModel: FundDetailViewModel) {
    val chartRange by viewModel.chartRange.collectAsStateWithLifecycle()
    val data = uiState.yieldDiagramData
    val indexName = uiState.yieldIndexName
    var viewMode by remember { mutableStateOf(ViewMode.LINE) }
    var searchDate by remember { mutableStateOf("") }

    Column {
        ChartRangeSelector(selectedRange = chartRange, onRangeChange = { viewModel.loadChart(it) })
        ViewModeSelector(mode = viewMode, onModeChange = { viewMode = it })

        if (data.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("暂无数据", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            return
        }

        DateSearchBar(searchDate = searchDate, onSearchChange = { searchDate = it }, dataSize = data.size)

        when (viewMode) {
            ViewMode.LINE -> CanvasLineChart(
                data = data.map { it.yield ?: 0.0 },
                labels = data.map { it.date ?: "" },
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            )
            ViewMode.BAR -> SimpleLineChart(
                data = data.map { it.yield ?: 0.0 },
                labels = data.map { it.date?.takeLast(5) ?: "" },
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            )
            ViewMode.LIST -> {
                val filtered = if (searchDate.isNotBlank()) {
                    data.filter { (it.date ?: "").contains(searchDate) }
                } else data
                LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    item {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text("日期", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text("涨幅", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            Text(indexName, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    }
                    items(filtered) { item ->
                        val y = item.yield ?: 0.0
                        val iy = item.indexYield ?: 0.0
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(item.date ?: "--", fontSize = 12.sp, modifier = Modifier.weight(1f))
                            Text("${String.format("%.2f", y)}%", fontSize = 12.sp, color = if (y >= 0) UpRed else DownGreen, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            Text("${String.format("%.2f", iy)}%", fontSize = 12.sp, color = if (iy >= 0) UpRed else DownGreen, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                        }
                    }
                }
            }
        }
    }
}

// 日期搜索栏
@Composable
fun DateSearchBar(searchDate: String, onSearchChange: (String) -> Unit, dataSize: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(4.dp))
        OutlinedTextField(
            value = searchDate,
            onValueChange = onSearchChange,
            placeholder = { Text("搜索日期 如01-15", fontSize = 11.sp) },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(180.dp).height(48.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("共$dataSize条", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// Canvas 真·折线图
@Composable
fun CanvasLineChart(data: List<Double>, labels: List<String>, modifier: Modifier = Modifier) {
    if (data.isEmpty()) return
    val minVal = data.min()
    val maxVal = data.max()
    val range = (maxVal - minVal).coerceAtLeast(0.01)
    val lineColor = if (data.last() >= data.first()) UpRed else DownGreen

    Card(modifier = modifier, shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("最高: ${String.format("%.4f", maxVal)}", fontSize = 10.sp, color = UpRed)
                Text("最低: ${String.format("%.4f", minVal)}", fontSize = 10.sp, color = DownGreen)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Canvas(modifier = Modifier.fillMaxWidth().height(220.dp)) {
                if (data.size < 2) return@Canvas
                val w = size.width
                val h = size.height
                val pad = 16f
                val drawW = w - pad * 2
                val drawH = h - pad * 2
                val stepX = drawW / (data.size - 1)

                // 填充区域
                val fillPath = Path()
                data.forEachIndexed { i, v ->
                    val x = pad + i * stepX
                    val y = pad + drawH * (1 - ((v - minVal) / range)).toFloat()
                    if (i == 0) { fillPath.moveTo(x, pad + drawH); fillPath.lineTo(x, y) }
                    else fillPath.lineTo(x, y)
                }
                fillPath.lineTo(pad + (data.size - 1) * stepX, pad + drawH)
                fillPath.close()
                drawPath(fillPath, color = lineColor.copy(alpha = 0.08f))

                // 折线
                val linePath = Path()
                data.forEachIndexed { i, v ->
                    val x = pad + i * stepX
                    val y = pad + drawH * (1 - ((v - minVal) / range)).toFloat()
                    if (i == 0) linePath.moveTo(x, y) else linePath.lineTo(x, y)
                }
                drawPath(linePath, color = lineColor, style = Stroke(width = 2.5f))

                // 末端圆点
                if (data.isNotEmpty()) {
                    val lastX = pad + (data.size - 1) * stepX
                    val lastY = pad + drawH * (1 - ((data.last() - minVal) / range)).toFloat()
                    drawCircle(lineColor, radius = 5f, center = Offset(lastX, lastY))
                }
            }
            // X轴标签（取样）
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                val sampleCount = minOf(6, labels.size)
                val step = if (sampleCount > 1) (labels.size - 1) / (sampleCount - 1) else 0
                for (i in 0 until sampleCount) {
                    val idx = (i * step).coerceAtMost(labels.size - 1)
                    Text(labels[idx].takeLast(5), fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

// 简易条形图
@Composable
fun SimpleLineChart(data: List<Double>, labels: List<String>, modifier: Modifier = Modifier) {
    if (data.isEmpty()) return
    val minVal = data.min()
    val maxVal = data.max()
    val range = if (maxVal - minVal == 0.0) 1.0 else maxVal - minVal

    Card(modifier = modifier, shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("最高: ${String.format("%.4f", maxVal)}  最低: ${String.format("%.4f", minVal)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            data.takeLast(20).forEachIndexed { index, value ->
                val realIndex = data.size - minOf(20, data.size) + index
                val label = if (realIndex < labels.size) labels[realIndex] else ""
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                    Box(modifier = Modifier.weight(2f).height(12.dp)) {
                        Box(
                            modifier = Modifier.fillMaxHeight()
                                .fillMaxWidth(((value - minVal) / range).toFloat())
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (value >= data.average()) UpRed.copy(alpha = 0.6f) else DownGreen.copy(alpha = 0.6f))
                        )
                    }
                    Text(String.format("%.4f", value), fontSize = 10.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                }
            }
        }
    }
}

// Tab 4: 基金概况
@Composable
fun FundInfoTab(uiState: FundDetailUiState) {
    val info = uiState.fundInfo
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (info != null) {
            item {
                Text("历史业绩排名", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    listOf("近1月" to (info.return1M to info.rank1M), "近3月" to (info.return3M to info.rank3M), "近6月" to (info.return6M to info.rank6M), "近1年" to (info.return1Y to info.rank1Y)).forEach { (label, pair) ->
                        val (value, rank) = pair
                        val color = if ((value ?: 0.0) >= 0) UpRed else DownGreen
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${value ?: "--"}%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
                            Text("排名: ${rank ?: "--"}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            item {
                HorizontalDivider()
                Text("基本信息", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(8.dp))
                InfoRow("基金代码", info.code ?: "--")
                InfoRow("基金类型", info.type ?: "--")
                InfoRow("基金公司", info.company ?: "--")
                InfoRow("基金经理", info.manager ?: "--")
                InfoRow("单位净值", "${info.nav ?: "--"} (${info.navDate ?: "--"})")
                InfoRow("累计净值", info.totalNav?.toString() ?: "--")
                InfoRow("交易状态", "${info.buyStatus ?: "--"} ${info.sellStatus ?: "--"}")
                info.scale?.let { InfoRow("基金规模", it.formatAmount()) }
                info.bonus?.let { bonus -> InfoRow("分红信息", "${bonus.date}日 每份折算${bonus.ratio}份") }
            }
        }
        if (uiState.managerHistory.isNotEmpty()) {
            item {
                HorizontalDivider()
                Text("基金经理变更记录", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(uiState.managerHistory) { manager ->
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(manager.name ?: "--", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                            Text(
                                if (manager.endDate.isNullOrEmpty()) "现任" else "已离任",
                                fontSize = 11.sp,
                                color = if (manager.endDate.isNullOrEmpty()) UpRed else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${manager.startDate} ~ ${manager.endDate ?: "至今"}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("任职${manager.days.toInt()}天", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("任职涨幅: ${String.format("%.2f", manager.growth)}%", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = if (manager.growth >= 0) UpRed else DownGreen)
                        manager.resume?.takeIf { it.isNotBlank() }?.let {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(it, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3)
                        }
                    }
                }
            }
        }
        if (info == null && uiState.managerHistory.isEmpty()) {
            item { Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { Text("加载中...", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 13.sp)
    }
}

@Composable
fun PositionDialog(currentShares: Double, onDismiss: () -> Unit, onAdd: (Double) -> Unit, onReduce: (Double) -> Unit) {
    var inputShares by remember { mutableStateOf("") }
    var isAdd by remember { mutableStateOf(true) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isAdd) "加仓" else "减仓") },
        text = {
            Column {
                Text("当前持有: ${String.format("%.2f", currentShares)} 份", fontSize = 14.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = isAdd, onClick = { isAdd = true }, label = { Text("加仓") })
                    FilterChip(selected = !isAdd, onClick = { isAdd = false }, label = { Text("减仓") })
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = inputShares, onValueChange = { inputShares = it }, label = { Text("份额") }, singleLine = true)
            }
        },
        confirmButton = { TextButton(onClick = { inputShares.toDoubleOrNull()?.let { if (isAdd) onAdd(it) else onReduce(it) } }) { Text("确定") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}
