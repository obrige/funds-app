package com.fundhelper.app.ui.market

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fundhelper.app.data.model.*
import com.fundhelper.app.data.repository.FundRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FlowRange(val label: String, val isIntraday: Boolean, val lmt: Int)

@HiltViewModel
class MarketViewModel @Inject constructor(
    private val repository: FundRepository
) : ViewModel() {

    private val _sectors = MutableStateFlow<List<SectorItem>>(emptyList())
    val sectors: StateFlow<List<SectorItem>> = _sectors.asStateFlow()

    // bkzj 板块数据
    private val _bkzjItems = MutableStateFlow<List<BkzjItem>>(emptyList())
    val bkzjItems: StateFlow<List<BkzjItem>> = _bkzjItems.asStateFlow()

    private val _bkType = MutableStateFlow(BkType.INDUSTRY)
    val bkType: StateFlow<BkType> = _bkType.asStateFlow()

    private val _bkPeriod = MutableStateFlow(BkPeriod.TODAY)
    val bkPeriod: StateFlow<BkPeriod> = _bkPeriod.asStateFlow()

    private val _bkInflow = MutableStateFlow(true) // true=流入f62, false=流出f164
    val bkInflow: StateFlow<Boolean> = _bkInflow.asStateFlow()

    private val _marketFlow = MutableStateFlow<List<String>>(emptyList())
    val marketFlow: StateFlow<List<String>> = _marketFlow.asStateFlow()

    private val _northFlow = MutableStateFlow<List<String>>(emptyList())
    val northFlow: StateFlow<List<String>> = _northFlow.asStateFlow()

    private val _southFlow = MutableStateFlow<List<String>>(emptyList())
    val southFlow: StateFlow<List<String>> = _southFlow.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _flowRange = MutableStateFlow(FlowRange("1日", true, 0))
    val flowRange: StateFlow<FlowRange> = _flowRange.asStateFlow()

    val flowRanges = listOf(FlowRange("1日", true, 0), FlowRange("3日", false, 3), FlowRange("5日", false, 5), FlowRange("10日", false, 10), FlowRange("20日", false, 20))

    fun loadAll() { viewModelScope.launch { _isLoading.value = true; launch { loadBkzj() }; launch { loadMarketFlow() }; launch { loadNorthSouthFlow() }; _isLoading.value = false } }
    fun setFlowRange(range: FlowRange) { _flowRange.value = range; viewModelScope.launch { if (range.isIntraday) _marketFlow.value = repository.getMarketFundFlow()?.data?.klines ?: emptyList() else _marketFlow.value = repository.getMarketFundFlowDay(range.lmt)?.data?.klines ?: emptyList() } }

    fun setBkType(type: BkType) { _bkType.value = type; loadBkzj() }
    fun setBkPeriod(period: BkPeriod) { _bkPeriod.value = period; loadBkzj() }
    fun toggleBkInflow() { _bkInflow.value = !_bkInflow.value; loadBkzj() }

    private fun loadBkzj() {
        viewModelScope.launch {
            val code = _bkType.value.codeParam + "+" + _bkPeriod.value.statCode
            val key = if (_bkInflow.value) "f62" else "f164"
            _bkzjItems.value = repository.getBkzj(key, code)
        }
    }

    private suspend fun loadMarketFlow() { val r = _flowRange.value; if (r.isIntraday) _marketFlow.value = repository.getMarketFundFlow()?.data?.klines ?: emptyList() else _marketFlow.value = repository.getMarketFundFlowDay(r.lmt)?.data?.klines ?: emptyList() }
    private suspend fun loadNorthSouthFlow() { val r = repository.getNorthSouthFlow(); _northFlow.value = r?.data?.s2n ?: emptyList(); _southFlow.value = r?.data?.n2s ?: emptyList() }
}
