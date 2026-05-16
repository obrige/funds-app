package com.fundhelper.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fundhelper.app.data.model.FundDisplayItem
import com.fundhelper.app.ui.theme.DownGreen
import com.fundhelper.app.ui.theme.UpRed
import com.fundhelper.app.util.*

@Composable
fun FundCard(
    item: FundDisplayItem,
    isEditing: Boolean,
    showGSZ: Boolean,
    showAmount: Boolean,
    showGains: Boolean,
    showCost: Boolean,
    showCostRate: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onToggleFavorite: () -> Unit,
    onSharesChange: (Double) -> Unit,
    onCostChange: (Double) -> Unit
) {
    val changeRate = item.fundData?.gszzl ?: 0.0
    val rateColor = if (changeRate >= 0) UpRed else DownGreen

    // 净值是否已更新（当日净值已出）
    val hasReplace = item.fundData?.pDate != null && item.fundData?.gzTime != null
            && item.fundData.pDate == item.fundData.gzTime.take(10)

    // 有效涨跌幅：已更新用 NAVCHGRT，否则用 GSZZL
    val effectiveRate = if (hasReplace) (item.fundData?.navChangeRate ?: changeRate) else changeRate
    val effectiveRateColor = if (effectiveRate >= 0) UpRed else DownGreen

    // 估值（用于第三行显示）
    val gsz = item.fundData?.gsz

    // 显示用净值：优先用 FundInfo 返回的 DWJZ，其次 FundData 的 NAV
    val displayNav = item.fundNav ?: item.fundData?.nav

    // 净值日期
    val navDateStr = item.navDate ?: item.fundData?.pDate ?: ""

    // 近一年收益率
    val return1Y = item.return1Y
    val return1YColor = if ((return1Y ?: 0.0) >= 0) UpRed else DownGreen

    Card(
        modifier = Modifier.fillMaxWidth().clickable(enabled = !isEditing) { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            // ========== 第一行：名称 + 代码 | 涨跌幅 ==========
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (item.entity.isFavorite) {
                        Icon(Icons.Default.Star, contentDescription = "特别关注", tint = UpRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        item.entity.name,
                        fontWeight = FontWeight.Medium, fontSize = 15.sp,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(item.entity.code, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                }
                Text(
                    effectiveRate.formatPercent(),
                    fontWeight = FontWeight.Bold, fontSize = 16.sp,
                    color = effectiveRateColor
                )
            }

            // ========== 第二行：近1年收益率 | 净值(日期) ==========
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("近1年 ", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        return1Y?.formatPercent() ?: "--",
                        fontSize = 13.sp, fontWeight = FontWeight.Medium,
                        color = return1YColor
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("净值 ", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        if (displayNav != null && displayNav > 0) String.format("%.4f", displayNav) else "--",
                        fontSize = 13.sp, fontWeight = FontWeight.Medium
                    )
                    if (navDateStr.isNotEmpty()) {
                        Text(" ($navDateStr)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    }
                }
            }

            // ========== 第三行：涨跌标记 + 估值 + 更新时间 ==========
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (hasReplace) "日涨跌 " else "估涨跌 ",
                        fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        effectiveRate.formatPercent(),
                        fontSize = 13.sp, fontWeight = FontWeight.Medium,
                        color = effectiveRateColor
                    )
                    if (hasReplace) {
                        Text(" (已更新)", fontSize = 10.sp, color = UpRed.copy(alpha = 0.7f))
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (showGSZ && !isEditing && gsz != null && gsz > 0) {
                        Text("估值 ", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(gsz.toString(), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("  ", fontSize = 11.sp)
                    }
                    Text(
                        item.fundData?.gzTime?.takeLast(8) ?: "--",
                        fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ========== 编辑模式 ==========
            if (isEditing) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = if (item.entity.shares > 0) item.entity.shares.toString() else "",
                        onValueChange = { value -> value.toDoubleOrNull()?.let { onSharesChange(it) } },
                        label = { Text("持有份额", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f), singleLine = true,
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = if (item.entity.costPrice > 0) item.entity.costPrice.toString() else "",
                        onValueChange = { value -> value.toDoubleOrNull()?.let { onCostChange(it) } },
                        label = { Text("成本价", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f), singleLine = true,
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            if (item.entity.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "特别关注",
                            tint = if (item.entity.isFavorite) UpRed else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                    }
                }
            }

            // ========== 持有额/收益行（非编辑模式） ==========
            if (!isEditing) {
                val hasExtraInfo = showAmount || showGains || showCost || showCostRate
                if (hasExtraInfo) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (showAmount) {
                            Column {
                                Text("持有额", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(item.holdingAmount.formatAmount(), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                        if (showGains) {
                            Column {
                                Text("估算收益", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(item.estimatedGain.formatGain(), fontSize = 12.sp, fontWeight = FontWeight.Medium, color = if (item.estimatedGain >= 0) UpRed else DownGreen)
                            }
                        }
                        if (showCost) {
                            Column {
                                Text("持有收益", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(item.costGain.formatGain(), fontSize = 12.sp, fontWeight = FontWeight.Medium, color = if (item.costGain >= 0) UpRed else DownGreen)
                            }
                        }
                        if (showCostRate && item.entity.costPrice > 0) {
                            Column {
                                Text("持有收益率", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(item.costGainRate.formatPercent(), fontSize = 12.sp, fontWeight = FontWeight.Medium, color = if (item.costGainRate >= 0) UpRed else DownGreen)
                            }
                        }
                    }
                }
            }
        }
    }
}
