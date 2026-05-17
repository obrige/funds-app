package com.fundhelper.app.ui.settings

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fundhelper.app.data.PrefsDataStore
import com.fundhelper.app.data.model.ExportData
import com.fundhelper.app.data.repository.FundRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
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

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val exportAdapter = moshi.adapter(ExportData::class.java)

    fun exportConfig(onResult: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val funds = repository.getAllFunds().first()
                val indices = repository.getAllIndices().first()
                val json = exportAdapter.toJson(ExportData(funds, indices))
                onResult(json)
            } catch (e: Exception) {
                onResult("导出失败: ${e.message}")
            }
        }
    }

    fun importConfig(json: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val data = exportAdapter.fromJson(json)
                if (data != null) {
                    if (data.funds.isNotEmpty()) {
                        repository.importFunds(data.funds)
                    }
                    if (data.indices.isNotEmpty()) {
                        // 清空现有指数再导入
                        val existing = repository.getAllIndices().first()
                        existing.forEach { repository.removeIndex(it.secId) }
                        data.indices.forEach { repository.addIndex(it) }
                    }
                    onResult("导入成功: ${data.funds.size}只基金, ${data.indices.size}个指数")
                } else {
                    onResult("数据格式错误，无法解析")
                }
            } catch (e: Exception) {
                onResult("导入失败: ${e.message}")
            }
        }
    }
}
