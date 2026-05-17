package com.fundhelper.app.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fundhelper.app.data.model.*
import com.fundhelper.app.data.repository.FundRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FundDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: FundRepository
) : ViewModel() {

    private val fundCode: String = savedStateHandle.get<String>("code") ?: ""

    private val _uiState = MutableStateFlow(FundDetailUiState())
    val uiState: StateFlow<FundDetailUiState> = _uiState.asStateFlow()

    private val _chartRange = MutableStateFlow("y")
    val chartRange: StateFlow<String> = _chartRange.asStateFlow()

    init { loadAll() }

    fun loadAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val entity = repository.getAllFunds().first().find { it.code == fundCode }
            _uiState.update { it.copy(fund = entity) }
            launch { loadFundData() }
            launch { loadFundInfo() }
            launch { loadTrend() }
            launch { loadChart(_chartRange.value) }
            launch { loadPosition() }
            launch { loadManager() }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private suspend fun loadFundData() {
        val data = repository.getFundRealtimeData(fundCode)
        _uiState.update { it.copy(fundData = data.firstOrNull()) }
    }

    private suspend fun loadFundInfo() {
        val response = repository.getFundInfo(fundCode)
        _uiState.update { it.copy(fundInfo = response?.Datas) }
    }

    private suspend fun loadTrend() {
        val response = repository.getFundTrend(fundCode)
        val trend = response?.Datas ?: emptyList()
        val dwjz = response?.Expansion?.dwjz ?: 0.0
        _uiState.update { it.copy(trendData = trend, trendDwjz = dwjz) }
    }

    fun loadChart(range: String) {
        _chartRange.value = range
        viewModelScope.launch {
            launch { loadNetDiagram(range) }
            launch { loadYieldDiagram(range) }
        }
    }

    private suspend fun loadNetDiagram(range: String) {
        val response = repository.getFundNetDiagram(fundCode, range)
        _uiState.update { it.copy(netDiagramData = response?.Datas ?: emptyList()) }
    }

    private suspend fun loadYieldDiagram(range: String) {
        val response = repository.getFundYieldDiagram(fundCode, range)
        _uiState.update {
            it.copy(
                yieldDiagramData = response?.Datas ?: emptyList(),
                yieldIndexName = response?.Expansion?.indexName ?: "沪深300"
            )
        }
    }

    private suspend fun loadPosition() {
        val response = repository.getFundPosition(fundCode)
        val stocks = response?.Datas?.fundStocks ?: emptyList()
        _uiState.update { it.copy(positions = stocks, positionDate = response?.Expansion) }
        if (stocks.isNotEmpty()) {
            val secIds = stocks.joinToString(",") { "${it.exchange}.${it.stockCode}" }
            try {
                val quotes = repository.getIndexQuotes(secIds)
                _uiState.update { it.copy(stocks = quotes) }
            } catch (_: Exception) {}
        }
    }

    private suspend fun loadManager() {
        val response = repository.getFundManager(fundCode)
        _uiState.update { it.copy(managerHistory = response?.Datas?.fundMManager ?: emptyList()) }
    }

    fun addPosition(shares: Double) {
        viewModelScope.launch {
            val current = _uiState.value.fund?.shares ?: 0.0
            repository.updateShares(fundCode, current + shares)
            val updated = repository.getAllFunds().first().find { it.code == fundCode }
            _uiState.update { it.copy(fund = updated) }
        }
    }

    fun reducePosition(shares: Double) {
        viewModelScope.launch {
            val current = _uiState.value.fund?.shares ?: 0.0
            repository.updateShares(fundCode, maxOf(0.0, current - shares))
            val updated = repository.getAllFunds().first().find { it.code == fundCode }
            _uiState.update { it.copy(fund = updated) }
        }
    }

    fun exportCsv(tabIndex: Int): String {
        val state = _uiState.value
        val name = state.fund?.name ?: fundCode
        return when (tabIndex) {
            0 -> {
                val dwjz = state.trendDwjz
                val header = "时间,估算涨跌幅(%),估算净值"
                val rows = state.trendData.takeLast(50).mapNotNull { item ->
                    val parts = item.split(",")
                    if (parts.size >= 3) {
                        val cr = parts[2].toDoubleOrNull() ?: 0.0
                        val en = if (dwjz > 0) dwjz * (1 + cr * 0.01) else 0.0
                        "${parts[0]},${"%.2f".format(cr)},${"%.4f".format(en)}"
                    } else null
                }
                "$name-估值走势\n$header\n${rows.joinToString("\n")}"
            }
            1 -> {
                val header = "股票名称,股票代码,涨跌幅(%),占比(%)"
                val rows = state.positions.map { stock ->
                    val q = state.stocks.find { it.code == stock.stockCode }
                    val cr = q?.changeRate ?: 0.0
                    "${stock.stockName},${stock.stockCode},${"%.2f".format(cr)},${"%.2f".format(stock.ratio)}"
                }
                "$name-持仓明细\n$header\n${rows.joinToString("\n")}"
            }
            2 -> {
                val header = "日期,单位净值,累计净值,涨跌幅(%)"
                val rows = state.netDiagramData.map { item ->
                    "${item.date ?: "--"},${item.nav?.toString() ?: "--"},${item.totalNav?.toString() ?: "--"},${item.changeRate ?: "0"}"
                }
                "$name-历史净值\n$header\n${rows.joinToString("\n")}"
            }
            3 -> {
                val header = "日期,涨幅(%),${state.yieldIndexName}(%)"
                val rows = state.yieldDiagramData.map { item ->
                    "${item.date ?: "--"},${"%.2f".format(item.yield ?: 0.0)},${"%.2f".format(item.indexYield ?: 0.0)}"
                }
                "$name-累计收益\n$header\n${rows.joinToString("\n")}"
            }
            4 -> {
                val info = state.fundInfo
                if (info != null) {
                    "$name-基金概况\n基金名称,${info.name ?: "--"}\n基金代码,${info.code ?: "--"}\n基金类型,${info.type ?: "--"}\n基金公司,${info.company ?: "--"}\n基金经理,${info.manager ?: "--"}\n成立日期,${info.navDate ?: "--"}\n基金规模,${info.scale?.let { "${it / 100000000}亿" } ?: "--"}\n近1月,${info.return1M?.let { "%.2f%%".format(it) } ?: "--"}\n近3月,${info.return3M?.let { "%.2f%%".format(it) } ?: "--"}\n近6月,${info.return6M?.let { "%.2f%%".format(it) } ?: "--"}\n近1年,${info.return1Y?.let { "%.2f%%".format(it) } ?: "--"}"
                } else ""
            }
            else -> ""
        }
    }
}
