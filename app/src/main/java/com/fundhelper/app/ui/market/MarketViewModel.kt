package com.fundhelper.app.ui.market

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fundhelper.app.data.model.*
import com.fundhelper.app.data.repository.FundRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FlowRange(val label: String, val klt: Int, val lmt: Int)

@HiltViewModel
class MarketViewModel @Inject constructor(
    private val repository: FundRepository
) : ViewModel() {

    private val _sectors = MutableStateFlow<List<SectorItem>>(emptyList())
    val sectors: StateFlow<List<SectorItem>> = _sectors.asStateFlow()

    private val _marketFlow = MutableStateFlow<List<String>>(emptyList())
    val marketFlow: StateFlow<List<String>> = _marketFlow.asStateFlow()

    private val _northFlow = MutableStateFlow<List<String>>(emptyList())
    val northFlow: StateFlow<List<String>> = _northFlow.asStateFlow()

    private val _southFlow = MutableStateFlow<List<String>>(emptyList())
    val southFlow: StateFlow<List<String>> = _southFlow.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _flowRange = MutableStateFlow(FlowRange("1日", 1, 0))
    val flowRange: StateFlow<FlowRange> = _flowRange.asStateFlow()

    val flowRanges = listOf(
        FlowRange("1日", 1, 0),
        FlowRange("3日", 101, 3),
        FlowRange("5日", 101, 5),
        FlowRange("10日", 101, 10),
        FlowRange("20日", 101, 20)
    )

    fun loadAll() {
        viewModelScope.launch {
            _isLoading.value = true
            launch { loadSectors() }
            launch { loadMarketFlow(_flowRange.value.klt, _flowRange.value.lmt) }
            launch { loadNorthSouthFlow() }
            _isLoading.value = false
        }
    }

    fun setFlowRange(range: FlowRange) {
        _flowRange.value = range
        viewModelScope.launch { loadMarketFlow(range.klt, range.lmt) }
    }

    private suspend fun loadSectors() { _sectors.value = repository.getSectors() }
    private suspend fun loadMarketFlow(klt: Int, lmt: Int) { _marketFlow.value = repository.getMarketFundFlow(klt, lmt)?.data?.klines ?: emptyList() }
    private suspend fun loadNorthSouthFlow() { val r = repository.getNorthSouthFlow(); _northFlow.value = r?.data?.s2n ?: emptyList(); _southFlow.value = r?.data?.n2s ?: emptyList() }
}
