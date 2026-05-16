package com.fundhelper.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fundhelper.app.data.PrefsDataStore
import com.fundhelper.app.data.model.*
import com.fundhelper.app.data.repository.FundRepository
import com.fundhelper.app.util.TradingTimeUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: FundRepository,
    private val prefs: PrefsDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val darkMode: Flow<Boolean> = prefs.darkMode
    val showGSZ: Flow<Boolean> = prefs.showGSZ
    val showAmount: Flow<Boolean> = prefs.showAmount
    val showGains: Flow<Boolean> = prefs.showGains
    val showCost: Flow<Boolean> = prefs.showCost
    val showCostRate: Flow<Boolean> = prefs.showCostRate

    private var currentFunds: List<FundEntity> = emptyList()

    init {
        initDefaults()
        observeFunds()
        observeIndices()
    }

    private fun initDefaults() {
        viewModelScope.launch {
            repository.initDefaultIndices()
        }
    }

    private fun observeFunds() {
        viewModelScope.launch {
            repository.getAllFunds().collect { funds ->
                currentFunds = funds
                refreshFundData()
            }
        }
    }

    private fun observeIndices() {
        viewModelScope.launch {
            repository.getAllIndices().collect { indices ->
                _uiState.update { it.copy(indices = indices.map { idx -> IndexDisplayItem(idx) }) }
                refreshIndexData()
            }
        }
    }

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            refreshFundData()
            refreshIndexData()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    tradingStatus = TradingTimeUtil.getTradingStatus()
                )
            }
        }
    }

    private suspend fun refreshFundData() {
        if (currentFunds.isEmpty()) {
            _uiState.update { it.copy(funds = emptyList(), totalGain = 0.0, totalGainRate = 0.0, totalAmount = 0.0) }
            return
        }

        val codes = currentFunds.joinToString(",") { it.code }
        val apiData = repository.getFundRealtimeData(codes)

        var totalAmount = 0.0
        var totalGain = 0.0

        val displayItems = currentFunds.map { entity ->
            val data = apiData.find { it.code == entity.code }
            val shares = entity.shares
            val nav = data?.nav ?: 0.0
            val gsz = data?.gsz ?: nav
            val gszzl = data?.gszzl ?: 0.0

            val hasReplace = data?.pDate != null && data.gzTime != null
                    && data.pDate == data.gzTime.take(10)

            val effectiveNav = if (hasReplace) nav else gsz
            val effectiveRate = if (hasReplace) (data?.navChangeRate ?: gszzl) else gszzl

            val holdingAmount = nav * shares
            val estimatedGain = if (shares > 0 && data != null) {
                if (hasReplace) {
                    (nav - nav / (1 + (data.navChangeRate ?: 0.0) * 0.01)) * shares
                } else {
                    (gsz - nav) * shares
                }
            } else 0.0

            val costGain = if (entity.costPrice > 0 && shares > 0) {
                (effectiveNav - entity.costPrice) * shares
            } else 0.0

            val costGainRate = if (entity.costPrice > 0) {
                (effectiveNav - entity.costPrice) / entity.costPrice * 100
            } else 0.0

            totalAmount += holdingAmount
            totalGain += estimatedGain

            FundDisplayItem(
                entity = entity,
                fundData = data,
                estimatedGain = estimatedGain,
                holdingAmount = holdingAmount,
                costGain = costGain,
                costGainRate = costGainRate
            )
        }

        val totalGainRate = if (totalAmount > 0) totalGain / totalAmount * 100 else 0.0

        _uiState.update {
            it.copy(
                funds = displayItems,
                totalAmount = totalAmount,
                totalGain = totalGain,
                totalGainRate = totalGainRate,
                tradingStatus = TradingTimeUtil.getTradingStatus()
            )
        }
    }

    private suspend fun refreshIndexData() {
        val indices = _uiState.value.indices
        if (indices.isEmpty()) return

        val secIds = indices.joinToString(",") { it.entity.secId }
        val quotes = repository.getIndexQuotes(secIds)

        _uiState.update { state ->
            state.copy(
                indices = state.indices.map { item ->
                    val quote = quotes.find { it.code == item.entity.code }
                    item.copy(quote = quote)
                }
            )
        }
    }

    fun addFund(code: String, name: String) {
        viewModelScope.launch {
            repository.addFund(code, name)
        }
    }

    fun removeFund(code: String) {
        viewModelScope.launch {
            repository.removeFund(code)
        }
    }

    fun toggleFavorite(code: String) {
        viewModelScope.launch {
            val fund = currentFunds.find { it.code == code }
            fund?.let {
                val newFav = !it.isFavorite
                if (newFav) {
                    currentFunds.filter { f -> f.isFavorite }.forEach { f ->
                        repository.updateFavorite(f.code, false)
                    }
                }
                repository.updateFavorite(code, newFav)
            }
        }
    }

    fun updateShares(code: String, shares: Double) {
        viewModelScope.launch {
            repository.updateShares(code, shares)
        }
    }

    fun updateCostPrice(code: String, cost: Double) {
        viewModelScope.launch {
            repository.updateCostPrice(code, cost)
        }
    }

    fun toggleEditing() {
        _uiState.update { it.copy(isEditing = !it.isEditing) }
    }

    fun reorderFunds(fromIndex: Int, toIndex: Int) {
        val currentList = _uiState.value.funds.toMutableList()
        if (fromIndex < currentList.size && toIndex < currentList.size) {
            val item = currentList.removeAt(fromIndex)
            currentList.add(toIndex, item)
            _uiState.update { it.copy(funds = currentList) }
            viewModelScope.launch {
                repository.reorderFunds(currentList.map { it.entity.code })
            }
        }
    }

    fun searchFunds(keyword: String, onResult: (List<FundSearchItem>) -> Unit) {
        viewModelScope.launch {
            val results = repository.searchFunds(keyword)
            onResult(results)
        }
    }

    fun removeIndex(secId: String) {
        viewModelScope.launch { repository.removeIndex(secId) }
    }

    fun addIndex(secId: String, name: String, code: String, market: Int) {
        viewModelScope.launch {
            repository.addIndex(IndexEntity(secId = secId, name = name, code = code, market = market))
        }
    }
}
