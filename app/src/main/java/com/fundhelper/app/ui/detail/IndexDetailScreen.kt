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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fundhelper.app.ui.theme.DownGreen
import com.fundhelper.app.ui.theme.UpRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndexDetailScreen(
    secId: String, name: String, code: String, onBack: () -> Unit,
    viewModel: IndexDetailViewModel = hiltViewModel()
) {
    val quote by viewModel.quote.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val klineData by viewModel.klineData.collectAsState()
    val klinePeriod by viewModel.klinePeriod.collectAsState()
    LaunchedEffect(secId) { viewModel.loadQuote(secId) }
    val changeRate = quote?.changeRate ?: 0.0; val rateColor = if (changeRate >= 0) UpRed else DownGreen
    val price = quote?.price ?: 0.0; val amount = quote?.amount ?: 0.0

    // K线数据解析: date,open,close,high,low,volume,amount,...
    val closePrices = remember(klineData) { klineData.mapNotNull { it.split(",").getOrNull(2)?.toDoubleOrNull() } }
    val klineDates = remember(klineData) { klineData.mapNotNull { it.split(",").getOrNull(0)?.takeLast(5) } }

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
                    FilterChip(
                        selected = klinePeriod == p,
                        onClick = { viewModel.setPeriod(secId, p) },
                        label = { Text(p.label, fontSize = 12.sp) }
                    )
                }
            }

            // K线图
            Spacer(Modifier.height(12.dp))
            if (closePrices.isNotEmpty()) {
                IndexKlineChart(closePrices, klineDates)
                // 最高最低
                Spacer(Modifier.height(4.dp))
                val hi = closePrices.max(); val lo = closePrices.min()
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("最高: %.2f".format(hi), fontSize = 11.sp, color = UpRed)
                    Text("最低: %.2f".format(lo), fontSize = 11.sp, color = DownGreen)
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
fun IndexKlineChart(data: List<Double>, labels: List<String>) {
    if (data.isEmpty()) return
    val minV = data.min(); val maxV = data.max(); val rng = (maxV - minV).coerceAtLeast(0.01)
    val lc = if (data.last() >= data.first()) UpRed else DownGreen
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(12.dp)) {
            Canvas(Modifier.fillMaxWidth().height(220.dp)) {
                if (data.size < 2) return@Canvas
                val w = size.width; val h = size.height; val pad = 16f; val dW = w - pad * 2; val dH = h - pad * 2; val sX = dW / (data.size - 1)
                val fp = Path(); data.forEachIndexed { i, v -> val x = pad + i * sX; val y = pad + dH * (1 - ((v - minV) / rng)).toFloat(); if (i == 0) { fp.moveTo(x, pad + dH); fp.lineTo(x, y) } else fp.lineTo(x, y) }; fp.lineTo(pad + (data.size - 1) * sX, pad + dH); fp.close(); drawPath(fp, lc.copy(alpha = 0.08f))
                val lp = Path(); data.forEachIndexed { i, v -> val x = pad + i * sX; val y = pad + dH * (1 - ((v - minV) / rng)).toFloat(); if (i == 0) lp.moveTo(x, y) else lp.lineTo(x, y) }; drawPath(lp, lc, style = Stroke(2.5f))
                if (data.isNotEmpty()) { val lx = pad + (data.size - 1) * sX; val ly = pad + dH * (1 - ((data.last() - minV) / rng)).toFloat(); drawCircle(lc, 5f, Offset(lx, ly)) }
            }
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                val sc = minOf(6, labels.size); val st = if (sc > 1) (labels.size - 1) / (sc - 1) else 0
                for (i in 0 until sc) { val idx = (i * st).coerceAtMost(labels.size - 1); Text(labels[idx], fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
    }
}

@Composable private fun DetailRow(label: String, value: String) { Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium) } }
