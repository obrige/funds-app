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

    init {
        loadAll()
    }

    fun loadAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val entity = repository.getAllFunds().first().find { it.code == fundCode }
            _uiState.update { it.copy(fund = entity) }

            launch { loadFundData() }
            launch { loadFundInfo() }
            launch { loadTrend() }
            launch { loadHistoryNav() }
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
        val trend = response?.Datas?.trend ?: emptyList()
        _uiState.update { it.copy(trendData = trend) }
    }

    private suspend fun loadHistoryNav() {
        val response = repository.getFundHistoryNav(fundCode)
        val list = response?.Datas?.LSJZList ?: emptyList()
        _uiState.update { it.copy(historyNav = list) }
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
        val history = response?.Datas?.fundMManager ?: emptyList()
        _uiState.update { it.copy(managerHistory = history) }
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
            val newShares = maxOf(0.0, current - shares)
            repository.updateShares(fundCode, newShares)
            val updated = repository.getAllFunds().first().find { it.code == fundCode }
            _uiState.update { it.copy(fund = updated) }
        }
    }
}
