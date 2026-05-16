package com.fundhelper.app.ui.detail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.fundhelper.app.data.model.IndexDisplayItem
import com.fundhelper.app.data.model.IndexQuoteItem
import com.fundhelper.app.ui.theme.DownGreen
import com.fundhelper.app.ui.theme.UpRed
import com.fundhelper.app.util.formatPercent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndexDetailScreen(
    item: IndexDisplayItem,
    onBack: () -> Unit
) {
    val quote = item.quote
    val changeRate = quote?.changeRate ?: 0.0
    val rateColor = if (changeRate >= 0) UpRed else DownGreen
    val price = quote?.price ?: 0.0
    val change = quote?.change ?: 0.0
    val amount = quote?.amount ?: 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(item.entity.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 指数代码
            Text(
                "${item.entity.code}.${item.entity.market}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 当前价格
            Text(
                price.toString(),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = rateColor
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 涨跌额 + 涨跌幅
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    change.formatPercent(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = rateColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    changeRate.formatPercent(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = rateColor
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 成交量
            if (amount > 0) {
                InfoRow("成交量", "%.2f亿".format(amount / 1_0000_0000))
            }

            // 价格走势简图
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "价格走势",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // 占位走势图（后续可接入真实历史K线数据）
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                PricePlaceholderChart(rateColor = rateColor)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 指数信息
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("指数信息", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoRow("指数名称", item.entity.name)
                    InfoRow("指数代码", "${item.entity.code}.${item.entity.market}")
                    InfoRow("最新价", price.toString())
                    InfoRow("涨跌额", change.toString())
                    InfoRow("涨跌幅", changeRate.formatPercent())
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun PricePlaceholderChart(rateColor: Color) {
    // 模拟走势折线
    val points = remember {
        listOf(0.7f, 0.65f, 0.72f, 0.68f, 0.75f, 0.8f, 0.78f, 0.82f, 0.85f, 0.8f,
            0.83f, 0.88f, 0.86f, 0.9f, 0.87f, 0.92f, 0.89f, 0.94f, 0.91f, 0.95f)
    }

    Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        if (points.size < 2) return@Canvas

        val width = size.width
        val height = size.height
        val strokeWidth = 2.5f
        val stepX = width / (points.size - 1)
        val maxVal = points.max()
        val minVal = points.min()
        val range = (maxVal - minVal).coerceAtLeast(0.01f)

        // 填充区域路径
        val fillPath = Path()
        points.forEachIndexed { i, v ->
            val x = i * stepX
            val y = height - ((v - minVal) / range) * (height - 40f) - 20f
            if (i == 0) fillPath.moveTo(x, height) else fillPath.lineTo(x, y)
        }
        fillPath.lineTo((points.size - 1) * stepX, height)
        fillPath.close()

        // 半透明填充
        drawPath(fillPath, color = rateColor.copy(alpha = 0.1f))

        // 折线
        val linePath = Path()
        points.forEachIndexed { i, v ->
            val x = i * stepX
            val y = height - ((v - minVal) / range) * (height - 40f) - 20f
            if (i == 0) linePath.moveTo(x, y) else linePath.lineTo(x, y)
        }
        drawPath(linePath, color = rateColor, style = Stroke(width = strokeWidth))
    }
}
