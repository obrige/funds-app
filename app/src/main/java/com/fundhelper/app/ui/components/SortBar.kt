package com.fundhelper.app.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fundhelper.app.data.model.SortField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortBar(
    currentSort: SortField,
    isAsc: Boolean,
    onSortChange: (SortField) -> Unit,
    showAmount: Boolean,
    showGains: Boolean,
    showCost: Boolean,
    showCostRate: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("排序:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(4.dp))

        val fields = mutableListOf(SortField.NAME, SortField.CHANGE_RATE)
        if (showAmount) fields.add(SortField.AMOUNT)
        if (showGains) fields.add(SortField.GAIN)
        if (showCost) fields.add(SortField.COST_GAIN)
        if (showCostRate) fields.add(SortField.COST_GAIN_RATE)

        fields.forEach { field ->
            FilterChip(
                selected = currentSort == field,
                onClick = { onSortChange(field) },
                label = {
                    Text(
                        field.label + if (currentSort == field) (if (isAsc) "↑" else "↓") else "",
                        fontSize = 11.sp
                    )
                },
                modifier = Modifier.height(28.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
        }
    }
}
