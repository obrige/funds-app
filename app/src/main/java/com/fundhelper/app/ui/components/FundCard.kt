package com.fundhelper.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fundhelper.app.data.model.FundDisplayItem
import com.fundhelper.app.ui.theme.DownGreen
import com.fundhelper.app.ui.theme.UpRed
import com.fundhelper.app.util.*

@Composable
fun FundCard(
    item: FundDisplayItem, isEditing: Boolean, showGSZ: Boolean, showAmount: Boolean,
    showGains: Boolean, showCost: Boolean, showCostRate: Boolean,
    onClick: () -> Unit, onDelete: () -> Unit, onToggleFavorite: () -> Unit,
    onSharesChange: (Double) -> Unit, onCostChange: (Double) -> Unit
) {
    val fd = item.fundData
    val hasReplace = fd?.pDate != null && fd.gzTime != null && fd.pDate == fd.gzTime.take(10)
    val effectiveRate = fd?.navChangeRate ?: 0.0
    val rateColor = if (effectiveRate >= 0) UpRed else DownGreen
    val displayNav = item.fundNav ?: fd?.nav
    val navDateStr = item.navDate ?: fd?.pDate ?: ""
    val gsz = fd?.gsz
    val gszzl = fd?.gszzl
    val return1Y = item.return1Y

    var sharesText by remember(item.entity.code, isEditing) {
        mutableStateOf(if (item.entity.shares > 0) item.entity.shares.toString().removeSuffix(".0").removeSuffix(".0") else "")
    }
    var costText by remember(item.entity.code, isEditing) {
        mutableStateOf(if (item.entity.costPrice > 0) item.entity.costPrice.toString().removeSuffix(".0").removeSuffix(".0") else "")
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(enabled = !isEditing) { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    if (item.entity.isFavorite) { Icon(Icons.Default.Star, "关注", tint = UpRed, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)) }
                    Text(item.entity.name, fontWeight = FontWeight.Medium, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                    Spacer(Modifier.width(6.dp))
                    Text(item.entity.code, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                }
                Text(effectiveRate.formatPercent(), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = rateColor)
            }
            Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("近1年 ", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(return1Y?.formatPercent() ?: "--", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = if ((return1Y ?: 0.0) >= 0) UpRed else DownGreen)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("净值 ", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(if (displayNav != null && displayNav > 0) String.format("%.4f", displayNav) else "--", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    if (navDateStr.isNotEmpty()) Text(" ($navDateStr)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                }
            }
            if (showGSZ && !isEditing) {
                Row(Modifier.fillMaxWidth().padding(top = 2.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("估值 ", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(if (gsz != null) gsz.toString() else (fd?.nav?.let { String.format("%.4f", it) } ?: "--"), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        if (hasReplace) Text(" (已更新)", fontSize = 10.sp, color = UpRed.copy(alpha = 0.7f))
                        if (gszzl != null) {
                            Spacer(Modifier.width(6.dp))
                            Text(gszzl.formatPercent(), fontSize = 12.sp, fontWeight = FontWeight.Medium, color = if (gszzl >= 0) UpRed else DownGreen)
                        }
                    }
                    Text(fd?.gzTime?.takeLast(5) ?: "--", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else if (!showGSZ && !isEditing) {
                Row(Modifier.fillMaxWidth().padding(top = 2.dp), horizontalArrangement = Arrangement.End) {
                    Text(fd?.gzTime?.takeLast(5) ?: "--", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (isEditing) {
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = sharesText,
                        onValueChange = { v ->
                            sharesText = v
                            v.toDoubleOrNull()?.let { onSharesChange(it) }
                        },
                        label = { Text("持有份额", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = costText,
                        onValueChange = { v ->
                            costText = v
                            v.toDoubleOrNull()?.let { onCostChange(it) }
                        },
                        label = { Text("成本价", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                    IconButton(onClick = onToggleFavorite) { Icon(if (item.entity.isFavorite) Icons.Default.Star else Icons.Default.StarBorder, "关注", tint = if (item.entity.isFavorite) UpRed else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp)) }
                    IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "删除", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp)) }
                }
            }
            if (!isEditing && (showAmount || showGains || showCost || showCostRate)) {
                Spacer(Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (showAmount) Column { Text("持有额", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(item.holdingAmount.formatAmount(), fontSize = 12.sp, fontWeight = FontWeight.Medium) }
                    if (showGains) Column { Text("估算收益", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(item.estimatedGain.formatGain(), fontSize = 12.sp, fontWeight = FontWeight.Medium, color = if (item.estimatedGain >= 0) UpRed else DownGreen) }
                    if (showCost) Column { Text("持有收益", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(item.costGain.formatGain(), fontSize = 12.sp, fontWeight = FontWeight.Medium, color = if (item.costGain >= 0) UpRed else DownGreen) }
                    if (showCostRate && item.entity.costPrice > 0) Column { Text("持有收益率", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(item.costGainRate.formatPercent(), fontSize = 12.sp, fontWeight = FontWeight.Medium, color = if (item.costGainRate >= 0) UpRed else DownGreen) }
                }
            }
        }
    }
}
