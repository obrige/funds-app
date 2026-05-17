package com.fundhelper.app.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fundhelper.app.data.model.IndexQuoteItem
import com.fundhelper.app.data.model.IndexKlineResponse
import com.fundhelper.app.data.repository.FundRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class KlinePeriod(val label: String, val klt: Int, val lmt: Int)

@HiltViewModel
class IndexDetailViewModel @Inject constructor(
    private val repository: FundRepository
) : ViewModel() {

    private val _quote = MutableStateFlow<IndexQuoteItem?>(null)
    val quote: StateFlow<IndexQuoteItem?> = _quote.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _klineData = MutableStateFlow<List<String>>(emptyList())
    val klineData: StateFlow<List<String>> = _klineData.asStateFlow()

    private val _klinePeriod = MutableStateFlow(KlinePeriod("日K", 101, 120))
    val klinePeriod: StateFlow<KlinePeriod> = _klinePeriod.asStateFlow()

    val klinePeriods = listOf(
        KlinePeriod("周K", 102, 60),
        KlinePeriod("月K", 103, 60),
        KlinePeriod("季K", 104, 40),
        KlinePeriod("年K", 106, 20)
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
        _klineData.value = response?.data?.klines ?: emptyList()
    }
}
