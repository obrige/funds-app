package com.fundhelper.app.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fundhelper.app.data.model.*
import com.fundhelper.app.ui.components.*
import com.fundhelper.app.ui.theme.UpRed
import com.fundhelper.app.util.TradingTimeUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onFundClick: (String) -> Unit,
    onSettingsClick: () -> Unit,
    onMarketClick: () -> Unit,
    onIndexClick: ((IndexDisplayItem) -> Unit)? = null,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val showGSZ by viewModel.showGSZ.collectAsStateWithLifecycle(initialValue = false)
    val showAmount by viewModel.showAmount.collectAsStateWithLifecycle(initialValue = false)
    val showGains by viewModel.showGains.collectAsStateWithLifecycle(initialValue = false)
    val showCost by viewModel.showCost.collectAsStateWithLifecycle(initialValue = false)
    val showCostRate by viewModel.showCostRate.collectAsStateWithLifecycle(initialValue = false)

    var showSearchDialog by remember { mutableStateOf(false) }
    var sortField by remember { mutableStateOf(SortField.CHANGE_RATE) }
    var sortAsc by remember { mutableStateOf(false) }

    val sortedFunds = remember(uiState.funds, sortField, sortAsc) {
        uiState.funds.let { list ->
            val sorted = when (sortField) {
                SortField.NAME -> list.sortedBy { it.entity.name }
                SortField.CHANGE_RATE -> list.sortedBy { it.fundData?.gszzl ?: 0.0 }
                SortField.AMOUNT -> list.sortedBy { it.holdingAmount }
                SortField.GAIN -> list.sortedBy { it.estimatedGain }
                SortField.COST_GAIN -> list.sortedBy { it.costGain }
                SortField.COST_GAIN_RATE -> list.sortedBy { it.costGainRate }
            }
            if (sortAsc) sorted else sorted.reversed()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("自选基金助手", style = MaterialTheme.typography.titleMedium)
                        Text(
                            TradingTimeUtil.getStatusText(uiState.tradingStatus),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (uiState.tradingStatus == TradingStatus.TRADING)
                                UpRed else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onMarketClick) {
                        Icon(Icons.Outlined.ShowChart, "行情中心")
                    }
                    IconButton(onClick = { showSearchDialog = true }) {
                        Icon(Icons.Default.Add, "添加基金")
                    }
                    IconButton(onClick = { viewModel.toggleEditing() }) {
                        Icon(
                            if (uiState.isEditing) Icons.Default.Done else Icons.Default.Edit,
                            if (uiState.isEditing) "完成" else "编辑"
                        )
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, "设置")
                    }
                }
            )
        },
        floatingActionButton = {
            if (!uiState.isEditing) {
                FloatingActionButton(
                    onClick = { viewModel.refreshAll() },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Refresh, "刷新")
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                IndexBar(
                    indices = uiState.indices,
                    isEditing = uiState.isEditing,
                    onRemoveIndex = { viewModel.removeIndex(it) },
                    onAddIndex = { secId, name, code, market ->
                        viewModel.addIndex(secId, name, code, market)
                    },
                    onIndexClick = onIndexClick
                )
            }

            if (uiState.totalAmount > 0 && !uiState.isEditing) {
                item {
                    TotalGainCard(
                        totalAmount = uiState.totalAmount,
                        totalGain = uiState.totalGain,
                        totalGainRate = uiState.totalGainRate
                    )
                }
            }

            item {
                SortBar(
                    currentSort = sortField,
                    isAsc = sortAsc,
                    onSortChange = { field ->
                        if (field == sortField) sortAsc = !sortAsc
                        else { sortField = field; sortAsc = false }
                    },
                    showAmount = showAmount,
                    showGains = showGains,
                    showCost = showCost,
                    showCostRate = showCostRate
                )
            }

            if (sortedFunds.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Outlined.Assessment,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "暂无自选基金\n点击右上角 + 添加",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }

            itemsIndexed(sortedFunds, key = { _, item -> item.entity.code }) { _, item ->
                FundCard(
                    item = item,
                    isEditing = uiState.isEditing,
                    showGSZ = showGSZ,
                    showAmount = showAmount,
                    showGains = showGains,
                    showCost = showCost,
                    showCostRate = showCostRate,
                    onClick = { onFundClick(item.entity.code) },
                    onDelete = { viewModel.removeFund(item.entity.code) },
                    onToggleFavorite = { viewModel.toggleFavorite(item.entity.code) },
                    onSharesChange = { shares -> viewModel.updateShares(item.entity.code, shares) },
                    onCostChange = { cost -> viewModel.updateCostPrice(item.entity.code, cost) }
                )
            }

            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }

    if (showSearchDialog) {
        SearchFundDialog(
            onDismiss = { showSearchDialog = false },
            onSearch = { keyword, callback -> viewModel.searchFunds(keyword, callback) },
            onAdd = { code, name ->
                viewModel.addFund(code, name)
                showSearchDialog = false
            }
        )
    }
}
