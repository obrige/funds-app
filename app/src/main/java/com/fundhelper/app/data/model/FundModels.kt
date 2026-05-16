package com.fundhelper.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ========== Room Entities ==========

@Entity(tableName = "funds")
data class FundEntity(
    @PrimaryKey val code: String,
    val name: String,
    val group: String = "默认分组",
    val sortOrder: Int = 0,
    val shares: Double = 0.0,
    val costPrice: Double = 0.0,
    val isFavorite: Boolean = false
)

@Entity(tableName = "indices")
data class IndexEntity(
    @PrimaryKey val secId: String,
    val name: String,
    val code: String,
    val market: Int = 1,
    val sortOrder: Int = 0
)

@Entity(tableName = "fund_groups")
data class GroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val sortOrder: Int = 0
)

// ========== API Response Models ==========

@JsonClass(generateAdapter = true)
data class FundListResponse(
    val Datas: List<FundDataItem>?,
    val ErrCode: Int = 0,
    val ErrMsg: String? = null
)

@JsonClass(generateAdapter = true)
data class FundDataItem(
    @Json(name = "FCODE") val code: String,
    @Json(name = "SHORTNAME") val name: String,
    @Json(name = "PDATE") val pDate: String?,
    @Json(name = "NAV") val nav: Double?,
    @Json(name = "NAVCHGRT") val navChangeRate: Double?,
    @Json(name = "GSZ") val gsz: Double?,
    @Json(name = "GSZZL") val gszzl: Double?,
    @Json(name = "GZTIME") val gzTime: String?,
    @Json(name = "LJJZ") val ljjz: Double?,
    @Json(name = "FTYPE") val fType: String?
)

@JsonClass(generateAdapter = true)
data class FundSearchResponse(
    val Datas: List<FundSearchItem>?,
    val ErrCode: Int = 0
)

@JsonClass(generateAdapter = true)
data class FundSearchItem(
    @Json(name = "CODE") val code: String,
    @Json(name = "NAME") val name: String,
    @Json(name = "STOCKMARKET") val market: String?,
    @Json(name = "FundBaseInfo") val baseInfo: FundBaseInfo?
)

@JsonClass(generateAdapter = true)
data class FundBaseInfo(
    @Json(name = "FTYPE") val type: String?
)

@JsonClass(generateAdapter = true)
data class IndexQuoteResponse(
    val data: IndexQuoteData?
)

@JsonClass(generateAdapter = true)
data class IndexQuoteData(
    val diff: List<IndexQuoteItem>?
)

@JsonClass(generateAdapter = true)
data class IndexQuoteItem(
    @Json(name = "f2") val price: Double?,
    @Json(name = "f3") val changeRate: Double?,
    @Json(name = "f4") val change: Double?,
    @Json(name = "f12") val code: String?,
    @Json(name = "f13") val market: Int?,
    @Json(name = "f14") val name: String?,
    @Json(name = "f6") val amount: Double?
)

@JsonClass(generateAdapter = true)
data class FundPositionResponse(
    val Datas: FundPositionData?,
    val Expansion: String?
)

@JsonClass(generateAdapter = true)
data class FundPositionData(
    val fundStocks: List<FundStockItem>?
)

@JsonClass(generateAdapter = true)
data class FundStockItem(
    @Json(name = "GPDM") val stockCode: String,
    @Json(name = "GPJC") val stockName: String,
    @Json(name = "JZBL") val ratio: Double,
    @Json(name = "NEWTEXCH") val exchange: String,
    @Json(name = "PC") val lastRatio: Double?
)

@JsonClass(generateAdapter = true)
data class FundInfoResponse(
    val Datas: FundInfoData?
)

@JsonClass(generateAdapter = true)
data class FundInfoData(
    @Json(name = "FCODE") val code: String?,
    @Json(name = "SHORTNAME") val name: String?,
    @Json(name = "FTYPE") val type: String?,
    @Json(name = "JJGS") val company: String?,
    @Json(name = "JJJL") val manager: String?,
    @Json(name = "DWJZ") val nav: Double?,
    @Json(name = "LJJZ") val totalNav: Double?,
    @Json(name = "FSRQ") val navDate: String?,
    @Json(name = "SGZT") val buyStatus: String?,
    @Json(name = "SHZT") val sellStatus: String?,
    @Json(name = "ENDNAV") val scale: Double?,
    @Json(name = "SYL_Y") val return1M: Double?,
    @Json(name = "SYL_3Y") val return3M: Double?,
    @Json(name = "SYL_6Y") val return6M: Double?,
    @Json(name = "SYL_1N") val return1Y: Double?,
    @Json(name = "RANKM") val rank1M: String?,
    @Json(name = "RANKQ") val rank3M: String?,
    @Json(name = "RANKHY") val rank6M: String?,
    @Json(name = "RANKY") val rank1Y: String?,
    @Json(name = "FUNDBONUS") val bonus: FundBonus?
)

@JsonClass(generateAdapter = true)
data class FundBonus(
    @Json(name = "PDATE") val date: String?,
    @Json(name = "CHGRATIO") val ratio: Double?
)

@JsonClass(generateAdapter = true)
data class FundManagerResponse(
    val Datas: FundManagerData?
)

@JsonClass(generateAdapter = true)
data class FundManagerData(
    val fundMManager: List<ManagerHistory>?,
    val MGRID: String?
)

@JsonClass(generateAdapter = true)
data class ManagerHistory(
    @Json(name = "MGRID") val id: String?,
    @Json(name = "MGRNAME") val name: String?,
    @Json(name = "FEMPDATE") val startDate: String?,
    @Json(name = "LEMPDATE") val endDate: String?,
    @Json(name = "DAYS") val days: Double,
    @Json(name = "PENAVGROWTH") val growth: Double,
    @Json(name = "PHOTOURL") val photoUrl: String?,
    @Json(name = "RESUME") val resume: String?
)

@JsonClass(generateAdapter = true)
data class FundTrendResponse(
    val Datas: FundTrendData?
)

@JsonClass(generateAdapter = true)
data class FundTrendData(
    @Json(name = "fundEstimate") val estimate: String?,
    val trend: List<List<String>>?
)

@JsonClass(generateAdapter = true)
data class FundHistoryNavResponse(
    val Datas: FundHistoryNavData?
)

@JsonClass(generateAdapter = true)
data class FundHistoryNavData(
    val LSJZList: List<HistoryNavItem>?
)

@JsonClass(generateAdapter = true)
data class HistoryNavItem(
    @Json(name = "FSRQ") val date: String,
    @Json(name = "DWJZ") val nav: String,
    @Json(name = "LJJZ") val totalNav: String,
    @Json(name = "JZZZL") val changeRate: String?
)

@JsonClass(generateAdapter = true)
data class SectorResponse(
    val data: SectorData?
)

@JsonClass(generateAdapter = true)
data class SectorData(
    val diff: List<SectorItem>?
)

@JsonClass(generateAdapter = true)
data class SectorItem(
    @Json(name = "f2") val price: Double?,
    @Json(name = "f3") val changeRate: Double?,
    @Json(name = "f12") val code: String?,
    @Json(name = "f14") val name: String?,
    @Json(name = "f62") val mainNetInflow: Double?,
    @Json(name = "f184") val mainNetInflowRate: Double?
)

@JsonClass(generateAdapter = true)
data class FundFlowResponse(
    val data: FundFlowData?
)

@JsonClass(generateAdapter = true)
data class FundFlowData(
    val diff: List<FundFlowItem>?
)

@JsonClass(generateAdapter = true)
data class FundFlowItem(
    @Json(name = "f62") val mainInflow: Double?,
    @Json(name = "f66") val superInflow: Double?,
    @Json(name = "f69") val superInflowRate: Double?,
    @Json(name = "f72") val bigInflow: Double?,
    @Json(name = "f75") val bigInflowRate: Double?,
    @Json(name = "f78") val midInflow: Double?,
    @Json(name = "f81") val midInflowRate: Double?,
    @Json(name = "f84") val smallInflow: Double?,
    @Json(name = "f87") val smallInflowRate: Double?,
    @Json(name = "f124") val timestamp: Long?
)

@JsonClass(generateAdapter = true)
data class NorthSouthFlowResponse(
    val data: NorthSouthFlowData?
)

@JsonClass(generateAdapter = true)
data class NorthSouthFlowData(
    val s2n: List<FlowItem>?,
    val n2s: List<FlowItem>?
)

@JsonClass(generateAdapter = true)
data class FlowItem(
    @Json(name = "f1") val time: Long?,
    @Json(name = "f2") val totalFlow: Double?,
    @Json(name = "f3") val shFlow: Double?,
    @Json(name = "f4") val szFlow: Double?
)

// ========== UI State Models ==========

data class FundDisplayItem(
    val entity: FundEntity,
    val fundData: FundDataItem? = null,
    val estimatedGain: Double = 0.0,
    val holdingAmount: Double = 0.0,
    val costGain: Double = 0.0,
    val costGainRate: Double = 0.0
)

data class IndexDisplayItem(
    val entity: IndexEntity,
    val quote: IndexQuoteItem? = null
)

data class HomeUiState(
    val funds: List<FundDisplayItem> = emptyList(),
    val indices: List<IndexDisplayItem> = emptyList(),
    val groups: List<GroupEntity> = emptyList(),
    val currentGroup: String = "全部",
    val isLoading: Boolean = false,
    val isEditing: Boolean = false,
    val tradingStatus: TradingStatus = TradingStatus.CLOSED,
    val totalGain: Double = 0.0,
    val totalGainRate: Double = 0.0,
    val totalAmount: Double = 0.0
)

enum class TradingStatus {
    NOT_STARTED, TRADING, LUNCH_BREAK, CLOSED, HOLIDAY
}

enum class SortField(val label: String) {
    NAME("名称"),
    CHANGE_RATE("涨跌幅"),
    AMOUNT("持有额"),
    GAIN("估算收益"),
    COST_GAIN("持有收益"),
    COST_GAIN_RATE("持有收益率")
}

data class FundDetailUiState(
    val fund: FundEntity? = null,
    val fundData: FundDataItem? = null,
    val fundInfo: FundInfoData? = null,
    val positions: List<FundStockItem> = emptyList(),
    val positionDate: String? = null,
    val stocks: List<IndexQuoteItem> = emptyList(),
    val managerHistory: List<ManagerHistory> = emptyList(),
    val trendData: List<List<String>> = emptyList(),
    val historyNav: List<HistoryNavItem> = emptyList(),
    val isLoading: Boolean = false
)


// ============ 图表数据模型 (与原项目 charts2.vue 一致) ============

// 历史净值图数据 (FundNetDiagram.ashx)
@JsonClass(generateAdapter = true)
data class FundNetDiagramResponse(
    val Datas: List<FundNetDiagramItem>?,
    val Expansion: NetDiagramExpansion?
)

@JsonClass(generateAdapter = true)
data class FundNetDiagramItem(
    @Json(name = "DWJZ") val nav: Double?,      // 单位净值
    @Json(name = "LJJZ") val totalNav: Double?,  // 累计净值
    @Json(name = "FSRQ") val date: String?,       // 日期
    @Json(name = "JZZZL") val changeRate: String? // 日增长率
)

@JsonClass(generateAdapter = true)
data class NetDiagramExpansion(
    @Json(name = "FCODE") val code: String?,
    @Json(name = "SHORTNAME") val name: String?
)

// 累计收益图数据 (FundYieldDiagramNew.ashx)
@JsonClass(generateAdapter = true)
data class FundYieldDiagramResponse(
    val Datas: List<FundYieldItem>?,
    val Expansion: YieldExpansion?
)

@JsonClass(generateAdapter = true)
data class FundYieldItem(
    @Json(name = "YIELD") val yield: Double?,        // 基金涨幅
    @Json(name = "INDEXYIED") val indexYield: Double?, // 对比指数涨幅
    @Json(name = "PDATE") val date: String?           // 日期
)

@JsonClass(generateAdapter = true)
data class YieldExpansion(
    @Json(name = "INDEXNAME") val indexName: String?,
    @Json(name = "FCODE") val code: String?
)

// 估值走势图数据 (FundVarietieValuationDetail.ashx)
// 原项目返回格式: Datas 是逗号分隔的字符串数组
// 但 Moshi 无法直接解析，需要用 ResponseBody 接收
// 所以 FundTrendResponse 保持不变，但 FundTrendData 需要调整
