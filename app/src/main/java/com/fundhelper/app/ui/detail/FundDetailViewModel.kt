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
}
