package com.fundhelper.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fundhelper.app.data.PrefsDataStore
import com.fundhelper.app.data.model.*
import com.fundhelper.app.data.repository.FundRepository
import com.fundhelper.app.util.TradingTimeUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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

        // 并行调用两个接口：fundgz（估值） + FundMNFInfo（实际净值/涨跌幅）
        val codes = currentFunds.joinToString(",") { it.code }
        val gzAndNav = coroutineScope {
            val gzDeferred = async {
                currentFunds.map { entity ->
                    async { entity to repository.getFundGz(entity.code) }
                }.awaitAll()
            }
            val navDeferred = async {
                try { repository.getFundRealtimeData(codes) } catch (_: Exception) { null }
            }
            gzDeferred.await() to navDeferred.await()
        }
        val (gzResults, navResponse) = gzAndNav

        // 构建 code → 实际NAVCHGRT 的映射
        val navMap = navResponse?.Datas?.associateBy { it.code } ?: emptyMap()

        var totalAmount = 0.0
        var totalGain = 0.0

        val displayItems = gzResults.map { (entity, response) ->
            // 优先从 FundMNFInfo 拿实际净值/涨跌幅，fallback 到 fundgz 估值
            val navItem = navMap[entity.code]
            val realNav = navItem?.nav ?: response?.dwjz?.toDoubleOrNull()
            val realNavChangeRate = navItem?.navChangeRate // NAVCHGRT 真实涨跌幅
            val pDate = navItem?.pDate ?: response?.jzrq
            val gsz = response?.gsz?.toDoubleOrNull()
            val gszzl = response?.gszzl?.toDoubleOrNull()
            val gzTime = response?.gztime

            val data = FundDataItem(
                code = entity.code,
                name = navItem?.name ?: response?.name ?: entity.name,
                pDate = pDate,
                nav = realNav,
                navChangeRate = realNavChangeRate,
                gsz = gsz,
                gszzl = gszzl,
                gzTime = gzTime,
                ljjz = null,
                fType = null
            )

            val shares = entity.shares
            val nav = data.nav ?: 0.0
            val gszVal = data.gsz ?: nav
            val gszzlVal = data.gszzl ?: 0.0
            val navChangeRate = data.navChangeRate ?: gszzlVal

            val hasReplace = data.pDate != null && data.gzTime != null
                    && data.pDate == data.gzTime.take(10)

            val effectiveNav = if (hasReplace) nav else gszVal
            val effectiveRate = if (hasReplace) navChangeRate else gszzlVal

            val amount = nav * shares
            val gains = if (shares > 0) {
                if (hasReplace) {
                    (nav - nav / (1 + navChangeRate * 0.01)) * shares
                } else {
                    (gszVal - nav) * shares
                }
            } else 0.0

            val costGains = if (entity.costPrice > 0 && shares > 0) {
                (effectiveNav - entity.costPrice) * shares
            } else 0.0

            val costGainsRate = if (entity.costPrice > 0) {
                (effectiveNav - entity.costPrice) / entity.costPrice * 100
            } else 0.0

            totalAmount += amount
            totalGain += gains

            FundDisplayItem(
                entity = entity,
                fundData = data,
                estimatedGain = gains,
                holdingAmount = amount,
                costGain = costGains,
                costGainRate = costGainsRate
            )
        }

        val totalNav = displayItems.sumOf { it.fundData?.nav ?: 0.0 }
        val totalGainRate = if (totalNav > 0) totalGain / (totalNav - totalGain) * 100 else 0.0

        _uiState.update {
            it.copy(
                funds = displayItems,
                totalGain = totalGain,
                totalGainRate = totalGainRate,
                totalAmount = totalAmount
            )
        }
    }

    private suspend fun refreshIndexData() {
        val indices = _uiState.value.indices
        if (indices.isEmpty()) return

        try {
            val secIds = indices.joinToString(",") { it.entity.secId }
            val response = repository.getIndexQuote(secIds)
            val items = response.data?.diff ?: return

            _uiState.update { state ->
                state.copy(
                    indices = indices.map { display ->
                        val match = items.find { it.code == display.entity.code }
                        if (match != null) display.copy(item = match) else display
                    }
                )
            }
        } catch (_: Exception) { }
    }

    fun addFund(fund: FundEntity) {
        viewModelScope.launch {
            repository.addFund(fund)
        }
    }

    fun removeFund(code: String) {
        viewModelScope.launch {
            repository.removeFund(code)
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

    fun toggleFavorite(code: String) {
        viewModelScope.launch {
            repository.toggleFavorite(code)
        }
    }
}
