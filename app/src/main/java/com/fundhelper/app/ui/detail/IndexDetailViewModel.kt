package com.fundhelper.app.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fundhelper.app.data.model.IndexQuoteItem
import com.fundhelper.app.data.repository.FundRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class KlinePeriod(val label: String, val klt: Int, val lmt: Int)
data class KlineChartData(
    val prices: List<Double>,
    val dates: List<String>,
    val allTimeHigh: Double,  // 加载数据范围内的最高点
    val currentPct: Double
)

@HiltViewModel
class IndexDetailViewModel @Inject constructor(
    private val repository: FundRepository
) : ViewModel() {

    private val _quote = MutableStateFlow<IndexQuoteItem?>(null)
    val quote: StateFlow<IndexQuoteItem?> = _quote.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _klinePeriod = MutableStateFlow(KlinePeriod("月K", 103, 500))
    val klinePeriod: StateFlow<KlinePeriod> = _klinePeriod.asStateFlow()

    private val _chartData = MutableStateFlow<KlineChartData?>(null)
    val chartData: StateFlow<KlineChartData?> = _chartData.asStateFlow()

    val klinePeriods = listOf(
        KlinePeriod("日K", 101, 250),
        KlinePeriod("周K", 102, 250),
        KlinePeriod("月K", 103, 500),
        KlinePeriod("季K", 104, 200),
        KlinePeriod("半年K", 105, 100),
        KlinePeriod("年K", 106, 50)
    )

    fun loadQuote(secId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val quotes = repository.getIndexQuotes(secId)
            _quote.value = quotes.firstOrNull()
            loadKline(secId)
            _isLoading.value = false
        }
    }

    fun setPeriod(secId: String, period: KlinePeriod) {
        _klinePeriod.value = period
        viewModelScope.launch { loadKline(secId) }
    }

    private suspend fun loadKline(secId: String) {
        val p = _klinePeriod.value
        val response = repository.getIndexKline(secId, p.klt, p.lmt)
        val raw = response?.data?.klines ?: emptyList()
        // 解析收盘价(字段2)和日期(字段0)
        val prices = raw.mapNotNull { it.split(",").getOrNull(2)?.toDoubleOrNull() }
        val dates = raw.mapNotNull { it.split(",").getOrNull(0)?.takeLast(5) }
        // 加载范围内的历史最高
        val allTimeHigh = prices.maxOrNull() ?: 0.0
        val currentPrice = _quote.value?.price ?: (prices.lastOrNull() ?: 0.0)
        val currentPct = if (allTimeHigh > 0) (currentPrice / allTimeHigh) * 100 else 100.0
        _chartData.value = KlineChartData(prices, dates, allTimeHigh, currentPct)
    }
}
