package com.fundhelper.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fundhelper.app.ui.theme.DownGreen
import com.fundhelper.app.ui.theme.UpRed
import com.fundhelper.app.util.*

@Composable
fun TotalGainCard(
    totalAmount: Double,
    totalGain: Double,
    totalGainRate: Double
) {
    val color = if (totalGain >= 0) UpRed else DownGreen

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("总持有额", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(totalAmount.formatAmount(), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("今日估算收益", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(totalGain.formatGain(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(totalGainRate.formatPercent(), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = color)
                }
            }
        }
    }
}
