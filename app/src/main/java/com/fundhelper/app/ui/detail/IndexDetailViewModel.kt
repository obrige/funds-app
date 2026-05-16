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

@HiltViewModel
class IndexDetailViewModel @Inject constructor(
    private val repository: FundRepository
) : ViewModel() {

    private val _quote = MutableStateFlow<IndexQuoteItem?>(null)
    val quote: StateFlow<IndexQuoteItem?> = _quote.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadQuote(secId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val quotes = repository.getIndexQuotes(secId)
            _quote.value = quotes.firstOrNull()
            _isLoading.value = false
        }
    }
}
