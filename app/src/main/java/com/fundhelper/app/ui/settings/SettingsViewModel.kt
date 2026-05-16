package com.fundhelper.app.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fundhelper.app.data.PrefsDataStore
import com.fundhelper.app.data.repository.FundRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    val prefs: PrefsDataStore,
    private val repository: FundRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    fun exportConfig() {
        viewModelScope.launch {
            val funds = repository.getAllFunds().first()
            val indices = repository.getAllIndices().first()
        }
    }

    fun importConfig() {
        viewModelScope.launch {
        }
    }
}
