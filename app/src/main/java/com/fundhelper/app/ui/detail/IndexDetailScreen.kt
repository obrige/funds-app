package com.fundhelper.app.ui.detail

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fundhelper.app.ui.theme.DownGreen
import com.fundhelper.app.ui.theme.UpRed
import com.fundhelper.app.util.formatPercent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndexDetailScreen(
    secId: String, name: String, code: String, onBack: () -> Unit,
    viewModel: IndexDetailViewModel = hiltViewModel()
) {
    val quote by viewModel.quote.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val klinePeriod by viewModel.klinePeriod.collectAsState()
    val chartData by viewModel.chartData.collectAsState()
    LaunchedEffect(secId) { viewModel.loadQuote(secId) }
    val changeRate = quote?.changeRate ?: 0.0; val rateColor = if (changeRate >= 0) UpRed else DownGreen
    val price = quote?.price ?: 0.0; val amount = quote?.amount ?: 0.0

    Scaffold(
        topBar = { TopAppBar(title = { Text(name) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "返回") } }, actions = { IconButton(onClick = { viewModel.loadQuote(secId) }) { Icon(Icons.Default.Refresh, "刷新") } }) }
    ) { padding ->
        if (isLoading) { Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }; return@Scaffold }
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(secId, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.height(8.dp))
            Text("%.2f".format(price), fontSize = 36.sp, fontWeight = FontWeight.Bold, color = rateColor); Spacer(Modifier.height(4.dp))
            Text(changeRate.formatPercent(), fontSize = 18.sp, fontWeight = FontWeight.Medium, color = rateColor)
            if (amount > 0) { Spacer(Modifier.height(8.dp)); Text("成交额 %.2f亿".format(amount / 1_0000_0000), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }

            // 周期选择
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                viewModel.klinePeriods.forEach { p ->
                    FilterChip(selected = klinePeriod == p, onClick = { viewModel.setPeriod(secId, p) }, label = { Text(p.label, fontSize = 12.sp) })
                }
            }

            // K线图 + 成交量
            Spacer(Modifier.height(12.dp))
            chartData?.let { cd ->
                if (cd.prices.isNotEmpty()) {
                    PercentileChart(cd)
                    Spacer(Modifier.height(8.dp))
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))) {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                            val vColor = when(cd.volumeStatus) { "放量" -> UpRed; "缩量" -> DownGreen; else -> MaterialTheme.colorScheme.onSurfaceVariant }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("量能判定", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(cd.volumeStatus, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = vColor) }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("平均成交", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(if (cd.avgVolume > 1e8) "%.2f亿".format(cd.avgVolume / 1e8) else "%.0f万".format(cd.avgVolume / 1e4), fontSize = 12.sp, fontWeight = FontWeight.Medium) }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("换手率", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(if (cd.turnoverRates.isNotEmpty()) "%.2f%%".format(cd.turnoverRates.last()) else "--", fontSize = 12.sp, fontWeight = FontWeight.Medium) }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                Column(Modifier.padding(12.dp)) {
                    Text("指数信息", fontWeight = FontWeight.Medium, fontSize = 14.sp); Spacer(Modifier.height(8.dp))
                    DetailRow("指数名称", name); DetailRow("指数代码", code)
                    DetailRow("最新价", "%.2f".format(price)); DetailRow("涨跌幅", changeRate.formatPercent())
                    quote?.let { q ->
                        if (q.high != null) DetailRow("最高", "%.2f".format(q.high))
                        if (q.low != null) DetailRow("最低", "%.2f".format(q.low))
                        if (q.open != null) DetailRow("开盘", "%.2f".format(q.open))
                        if (q.prevClose != null) DetailRow("昨收", "%.2f".format(q.prevClose))
                        if (q.amplitude != null) DetailRow("振幅", "%.2f%%".format(q.amplitude))
                        if (q.volumeRatio != null) DetailRow("量比", "%.2f".format(q.volumeRatio))
                        if (q.totalCap != null && q.totalCap > 0) DetailRow("总市值", if (q.totalCap > 1e12) "%.2f万亿".format(q.totalCap / 1e12) else "%.0f亿".format(q.totalCap / 1e8))
                        if (q.circCap != null && q.circCap > 0) DetailRow("流通市值", if (q.circCap > 1e12) "%.2f万亿".format(q.circCap / 1e12) else "%.0f亿".format(q.circCap / 1e8))
                    }
                }
            }
        }
    }
}

@Composable
fun PercentileChart(data: KlineChartData) {
    val pcts = remember(data) { data.prices.map { if (data.allTimeHigh > 0) (it / data.allTimeHigh) * 100 else 100.0 } }
    val currentPct = data.currentPct
    val color = if (currentPct >= 50) UpRed else DownGreen
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("历史最高: %.2f".format(data.allTimeHigh), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("当前: %.0f%%".format(currentPct), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(Modifier.height(4.dp))
        val chartWidth = (pcts.size * 8).coerceAtLeast(300).dp
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.horizontalScroll(rememberScrollState()).padding(12.dp)) {
                Canvas(Modifier.width(chartWidth).height(200.dp)) {
                    if (pcts.size < 2) return@Canvas
                    val w = size.width; val h = size.height; val pad = 12f; val dW = w - pad * 2; val dH = h - pad * 2
                    val sX = dW / (pcts.size - 1)
                    val midY = pad + dH * 0.5f
                    drawLine(Color.Gray.copy(alpha = 0.3f), Offset(pad, midY), Offset(pad + dW, midY), 1f)
                    pcts.forEachIndexed { i, pct ->
                        val x = pad + i * sX
                        val y = pad + dH * (1f - (pct / 100f).toFloat())
                        val barColor = if (pct >= 50) UpRed.copy(alpha = 0.6f) else DownGreen.copy(alpha = 0.6f)
                        val barHeight = kotlin.math.abs(y - midY).coerceAtLeast(2f)
                        val barTop = if (pct >= 50) y else midY
                        drawRect(barColor, Offset(x - 1.5f, barTop), Size(3f, barHeight))
                    }
                    val lp = Path()
                    pcts.forEachIndexed { i, pct -> val x = pad + i * sX; val y = pad + dH * (1f - (pct / 100f).toFloat()); if (i == 0) lp.moveTo(x, y) else lp.lineTo(x, y) }
                    drawPath(lp, color, style = Stroke(2f))
                    val lx = pad + (pcts.size - 1) * sX; val ly = pad + dH * (1f - (pcts.last() / 100f).toFloat())
                    drawCircle(color, 5f, Offset(lx, ly))
                }
                Spacer(Modifier.height(4.dp))
                Row(Modifier.width(chartWidth), horizontalArrangement = Arrangement.SpaceBetween) {
                    data.dates.firstOrNull()?.let { Text(it, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    data.dates.getOrNull(data.dates.size / 2)?.let { Text(it, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    data.dates.lastOrNull()?.let { Text(it, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }
        }
    }
}

@Composable private fun DetailRow(label: String, value: String) { Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium) } }
