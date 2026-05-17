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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
            if (amount > 0) { Spacer(Modifier.height(12.dp)); Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))) { Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text("成交量", fontSize = 13.sp); Text("%.2f亿".format(amount / 1_0000_0000), fontSize = 13.sp, fontWeight = FontWeight.Medium) } } }

            // 周期选择
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                viewModel.klinePeriods.forEach { p ->
                    FilterChip(selected = klinePeriod == p, onClick = { viewModel.setPeriod(secId, p) }, label = { Text(p.label, fontSize = 12.sp) })
                }
            }

            // 百分比K线图
            Spacer(Modifier.height(12.dp))
            chartData?.let { cd ->
                if (cd.prices.isNotEmpty()) {
                    PercentileChart(cd)
                }
            }

            Spacer(Modifier.height(24.dp))
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                Column(Modifier.padding(12.dp)) { Text("指数信息", fontWeight = FontWeight.Medium, fontSize = 14.sp); Spacer(Modifier.height(8.dp)); DetailRow("指数名称", name); DetailRow("指数代码", code); DetailRow("市场代码", secId); DetailRow("最新价", "%.2f".format(price)); DetailRow("涨跌幅", changeRate.formatPercent()) }
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
        // 当前百分位和基准
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("历史最高: %.2f".format(data.allTimeHigh), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("当前: %.0f%%".format(currentPct), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(Modifier.height(4.dp))
        // 可滑动的折线图
        val chartWidth = (pcts.size * 8).coerceAtLeast(300).dp
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.horizontalScroll(rememberScrollState()).padding(12.dp)) {
                Canvas(Modifier.width(chartWidth).height(200.dp)) {
                    if (pcts.size < 2) return@Canvas
                    val w = size.width; val h = size.height; val pad = 12f; val dW = w - pad * 2; val dH = h - pad * 2
                    val sX = dW / (pcts.size - 1)
                    // 50% 基准线
                    val midY = pad + dH * 0.5f
                    drawLine(Color.Gray.copy(alpha = 0.3f), Offset(pad, midY), Offset(pad + dW, midY), 1f)
                    // 填充区域: >=50%红色, <50%绿色
                    pcts.forEachIndexed { i, pct ->
                        val x = pad + i * sX
                        val y = pad + dH * (1f - (pct / 100f).toFloat())
                        val barColor = if (pct >= 50) UpRed.copy(alpha = 0.6f) else DownGreen.copy(alpha = 0.6f)
                        val barHeight = kotlin.math.abs(y - midY).coerceAtLeast(2f)
                        val barTop = if (pct >= 50) y else midY
                        drawRect(barColor, Offset(x - 1.5f, barTop), Size(3f, barHeight))
                    }
                    // 折线
                    val lp = Path()
                    pcts.forEachIndexed { i, pct ->
                        val x = pad + i * sX
                        val y = pad + dH * (1f - (pct / 100f).toFloat())
                        if (i == 0) lp.moveTo(x, y) else lp.lineTo(x, y)
                    }
                    drawPath(lp, color, style = Stroke(2f))
                    // 当前值圆点
                    val lx = pad + (pcts.size - 1) * sX
                    val ly = pad + dH * (1f - (pcts.last() / 100f).toFloat())
                    drawCircle(color, 5f, Offset(lx, ly))
                }
                // 日期标签
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
