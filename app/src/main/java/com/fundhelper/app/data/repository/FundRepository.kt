package com.fundhelper.app.data.repository

import com.fundhelper.app.data.api.FundApi
import com.fundhelper.app.data.db.FundDao
import com.fundhelper.app.data.db.GroupDao
import com.fundhelper.app.data.db.IndexDao
import com.fundhelper.app.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FundRepository @Inject constructor(
    private val fundApi: FundApi,
    private val fundDao: FundDao,
    private val indexDao: IndexDao,
    private val groupDao: GroupDao
) {
    // ========== Fund Operations ==========
    fun getAllFunds(): Flow<List<FundEntity>> = fundDao.getAllFunds()

    fun getFundsByGroup(group: String): Flow<List<FundEntity>> =
        if (group == "全部") fundDao.getAllFunds()
        else fundDao.getFundsByGroup(group)

    suspend fun addFund(code: String, name: String, group: String = "默认分组") {
        val existing = fundDao.getFundByCode(code)
        if (existing == null) {
            val count = fundDao.getFundCount()
            fundDao.insertFund(FundEntity(code = code, name = name, group = group, sortOrder = count))
        }
    }

    suspend fun removeFund(code: String) = fundDao.deleteFundByCode(code)
    suspend fun updateFund(fund: FundEntity) = fundDao.updateFund(fund)
    suspend fun updateShares(code: String, shares: Double) = fundDao.updateShares(code, shares)
    suspend fun updateCostPrice(code: String, cost: Double) = fundDao.updateCostPrice(code, cost)
    suspend fun updateFavorite(code: String, fav: Boolean) = fundDao.updateFavorite(code, fav)
    suspend fun getFavoriteFund() = fundDao.getFavoriteFund()

    suspend fun reorderFunds(codes: List<String>) {
        codes.forEachIndexed { index, code -> fundDao.updateOrder(code, index) }
    }

    suspend fun importFunds(funds: List<FundEntity>) = fundDao.insertFunds(funds)

    // ========== Fund Data (API) ==========
    suspend fun searchFunds(keyword: String): List<FundSearchItem> {
        return try {
            val response = fundApi.searchFund(keyword = keyword)
            response.Datas ?: emptyList()
        } catch (e: Exception) { emptyList() }
    }

    suspend fun getFundRealtimeData(codes: String): List<FundDataItem> {
        if (codes.isBlank()) return emptyList()
        return try {
            val response = fundApi.getFundRealtimeData(codes = codes)
            response.Datas ?: emptyList()
        } catch (e: Exception) { emptyList() }
    }

    // 估值走势图 (FundVarietieValuationDetail.ashx)
    suspend fun getFundTrend(code: String): FundTrendResponse? {
        return try { fundApi.getFundTrend(code = code) } catch (e: Exception) { null }
    }

    // 历史净值图 (FundNetDiagram.ashx) - 支持时间范围
    suspend fun getFundNetDiagram(code: String, range: String = "y"): FundNetDiagramResponse? {
        return try { fundApi.getFundNetDiagram(code = code, range = range) } catch (e: Exception) { null }
    }

    // 累计收益图 (FundYieldDiagramNew.ashx) - 支持时间范围
    suspend fun getFundYieldDiagram(code: String, range: String = "y"): FundYieldDiagramResponse? {
        return try { fundApi.getFundYieldDiagram(code = code, range = range) } catch (e: Exception) { null }
    }

    // 历史净值列表 (FundMNHisNetList)
    suspend fun getFundHistoryNav(code: String): FundHistoryNavResponse? {
        return try { fundApi.getFundHistoryNav(code = code) } catch (e: Exception) { null }
    }

    suspend fun getFundPosition(code: String): FundPositionResponse? {
        return try { fundApi.getFundPosition(code = code) } catch (e: Exception) { null }
    }

    suspend fun getFundInfo(code: String): FundInfoResponse? {
        return try { fundApi.getFundInfo(code = code) } catch (e: Exception) { null }
    }

    suspend fun getFundManager(code: String): FundManagerResponse? {
        return try { fundApi.getFundManager(code = code) } catch (e: Exception) { null }
    }

    // ========== Index Operations ==========
    fun getAllIndices(): Flow<List<IndexEntity>> = indexDao.getAllIndices()

    suspend fun addIndex(index: IndexEntity) {
        if (indexDao.getCount() < 4) { indexDao.insertIndex(index) }
    }

    suspend fun removeIndex(secId: String) = indexDao.deleteBySecId(secId)

    suspend fun initDefaultIndices() {
        if (indexDao.getCount() == 0) {
            indexDao.insertIndices(listOf(
                IndexEntity(secId = "1.000001", name = "上证指数", code = "000001", market = 1),
                IndexEntity(secId = "0.399001", name = "深证成指", code = "399001", market = 0),
                IndexEntity(secId = "0.399006", name = "创业板指", code = "399006", market = 0)
            ))
        }
    }

    suspend fun getIndexQuotes(secIds: String): List<IndexQuoteItem> {
        return try {
            val response = fundApi.getIndexQuote(secIds = secIds)
            response.data?.diff ?: emptyList()
        } catch (e: Exception) { emptyList() }
    }

    // ========== Group Operations ==========
    fun getAllGroups(): Flow<List<GroupEntity>> = groupDao.getAllGroups()
    suspend fun addGroup(name: String): Long = groupDao.insertGroup(GroupEntity(name = name))
    suspend fun deleteGroup(group: GroupEntity) = groupDao.deleteGroup(group)

    // ========== Market Center ==========
    suspend fun getSectors(): List<SectorItem> {
        return try { fundApi.getSectors().data?.diff ?: emptyList() } catch (e: Exception) { emptyList() }
    }

    suspend fun getMarketFundFlow(): FundFlowResponse? {
        return try { fundApi.getMarketFundFlow() } catch (e: Exception) { null }
    }

    suspend fun getNorthSouthFlow(): NorthSouthFlowResponse? {
        return try { fundApi.getNorthSouthFlow() } catch (e: Exception) { null }
    }
}
