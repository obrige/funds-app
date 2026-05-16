package com.fundhelper.app.ui.detail

import androidx.compose.foundation.Canvas
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
    secId: String,
    name: String,
    code: String,
    onBack: () -> Unit,
    viewModel: IndexDetailViewModel = hiltViewModel()
) {
    val quote by viewModel.quote.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(secId) { viewModel.loadQuote(secId) }

    val changeRate = quote?.changeRate ?: 0.0
    val rateColor = if (changeRate >= 0) UpRed else DownGreen
    val price = quote?.price ?: 0.0
    val change = quote?.change ?: 0.0
    val amount = quote?.amount ?: 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(name) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "返回") } },
                actions = { IconButton(onClick = { viewModel.loadQuote(secId) }) { Icon(Icons.Default.Refresh, "刷新") } }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(secId, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))
            Text(if (price > 0) price.toString() else "--", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = rateColor)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(changeRate.formatPercent(), fontSize = 18.sp, fontWeight = FontWeight.Medium, color = rateColor)
            }
            if (amount > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))) {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("成交量", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("%.2f亿".format(amount / 1_0000_0000), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("价格走势", fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { PricePlaceholderChart(rateColor = rateColor) }
            Spacer(modifier = Modifier.height(24.dp))
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("指数信息", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    DetailRow("指数名称", name)
                    DetailRow("指数代码", code)
                    DetailRow("市场代码", secId)
                    DetailRow("最新价", if (price > 0) price.toString() else "--")
                    DetailRow("涨跌幅", changeRate.formatPercent())
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun PricePlaceholderChart(rateColor: Color) {
    val points = remember { listOf(0.7f, 0.65f, 0.72f, 0.68f, 0.75f, 0.8f, 0.78f, 0.82f, 0.85f, 0.8f, 0.83f, 0.88f, 0.86f, 0.9f, 0.87f, 0.92f, 0.89f, 0.94f, 0.91f, 0.95f) }
    Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        if (points.size < 2) return@Canvas
        val w = size.width; val h = size.height; val sw = 2.5f; val stepX = w / (points.size - 1)
        val maxV = points.max(); val minV = points.min(); val rng = (maxV - minV).coerceAtLeast(0.01f)
        val fp = Path()
        points.forEachIndexed { i, v ->
            val x = i * stepX; val y = h - ((v - minV) / rng) * (h - 40f) - 20f
            if (i == 0) fp.moveTo(x, h) else fp.lineTo(x, y)
        }
        fp.lineTo((points.size - 1) * stepX, h); fp.close()
        drawPath(fp, color = rateColor.copy(alpha = 0.1f))
        val lp = Path()
        points.forEachIndexed { i, v ->
            val x = i * stepX; val y = h - ((v - minV) / rng) * (h - 40f) - 20f
            if (i == 0) lp.moveTo(x, y) else lp.lineTo(x, y)
        }
        drawPath(lp, color = rateColor, style = Stroke(width = sw))
    }
}
