package com.fundhelper.app.data.api

import com.fundhelper.app.data.model.*
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface FundApi {

    // 获取基金实时估值数据 (与原项目 App.vue getData 一致)
    @GET("FundMNewApi/FundMNFInfo")
    suspend fun getFundRealtimeData(
        @Query("Fcodes") codes: String,
        @Query("pageIndex") pageIndex: Int = 1,
        @Query("pageSize") pageSize: Int = 200,
        @Query("plat") plat: String = "Android",
        @Query("appType") appType: String = "ttjj",
        @Query("product") product: String = "EFund",
        @Query("Version") version: String = "1",
        @Query("deviceid") deviceId: String = "android_fund_helper"
    ): FundListResponse

    // 搜索基金 (与原项目 App.vue remoteMethod 一致: m=9, fundsuggest域名)
    @GET
    suspend fun searchFund(
        @Url url: String = "https://fundsuggest.eastmoney.com/FundSearch/api/FundSearchAPI.ashx",
        @Query("m") m: Int = 9,
        @Query("key") keyword: String,
        @Query("_") timestamp: Long = System.currentTimeMillis()
    ): FundSearchResponse

    // 获取基金估值走势图 (与原项目 charts.vue 一致)
    @GET("FundMNewApi/FundMNIVariablePrice")
    suspend fun getFundTrend(
        @Query("FCODE") code: String,
        @Query("RANGE") range: String = "y",
        @Query("deviceid") deviceId: String = "Wap",
        @Query("plat") plat: String = "Wap",
        @Query("product") product: String = "EFund",
        @Query("version") version: String = "2.0.0",
        @Query("_") timestamp: Long = System.currentTimeMillis()
    ): FundTrendResponse

    // 获取基金历史净值 (与原项目 charts2.vue 一致)
    @GET("FundMNewApi/FundMNHisNetList")
    suspend fun getFundHistoryNav(
        @Query("FCODE") code: String,
        @Query("pageIndex") pageIndex: Int = 1,
        @Query("pageSize") pageSize: Int = 30,
        @Query("deviceid") deviceId: String = "Wap",
        @Query("plat") plat: String = "Wap",
        @Query("product") product: String = "EFund",
        @Query("version") version: String = "2.0.0",
        @Query("_") timestamp: Long = System.currentTimeMillis()
    ): FundHistoryNavResponse

    // 获取基金持仓明细 (与原项目 positionDetail.vue 一致)
    @GET("FundMNewApi/FundMNInverstPosition")
    suspend fun getFundPosition(
        @Query("FCODE") code: String,
        @Query("deviceid") deviceId: String = "Wap",
        @Query("plat") plat: String = "Wap",
        @Query("product") product: String = "EFund",
        @Query("version") version: String = "2.0.0",
        @Query("_") timestamp: Long = System.currentTimeMillis()
    ): FundPositionResponse

    // 获取基金概况 (与原项目 fundInfo.vue 一致)
    @GET("FundMApi/FundBaseTypeInformation.ashx")
    suspend fun getFundInfo(
        @Query("FCODE") code: String,
        @Query("deviceid") deviceId: String = "Wap",
        @Query("plat") plat: String = "Wap",
        @Query("product") product: String = "EFund",
        @Query("version") version: String = "2.0.0",
        @Query("_") timestamp: Long = System.currentTimeMillis()
    ): FundInfoResponse

    // 获取基金经理 (与原项目 managerDetail.vue 一致)
    @GET("FundMNewApi/FundMNInfoNew")
    suspend fun getFundManager(
        @Query("FCODE") code: String,
        @Query("deviceid") deviceId: String = "Wap",
        @Query("plat") plat: String = "Wap",
        @Query("product") product: String = "EFund",
        @Query("version") version: String = "2.0.0",
        @Query("_") timestamp: Long = System.currentTimeMillis()
    ): FundManagerResponse

    // 获取指数实时行情 (与原项目 getIndFundData 一致)
    @GET
    suspend fun getIndexQuote(
        @Url url: String = "https://push2.eastmoney.com/api/qt/ulist.np/get",
        @Query("fltt") fltt: Int = 2,
        @Query("fields") fields: String = "f2,f3,f4,f6,f12,f13,f14",
        @Query("secids") secIds: String,
        @Query("_") timestamp: Long = System.currentTimeMillis()
    ): IndexQuoteResponse

    // 行业板块 (与原项目 marketBar.vue 一致: fs=m:90+t:2, fid=f62)
    @GET("https://push2.eastmoney.com/api/qt/clist/get")
    suspend fun getSectors(
        @Query("pn") pn: Int = 1,
        @Query("pz") pz: Int = 500,
        @Query("po") po: Int = 1,
        @Query("np") np: Int = 1,
        @Query("fields") fields: String = "f12,f13,f14,f62",
        @Query("fid") fid: String = "f62",
        @Query("fs") fs: String = "m:90+t:2",
        @Query("_") timestamp: Long = System.currentTimeMillis()
    ): SectorResponse

    // 大盘资金流向 (与原项目 marketLine.vue 一致)
    @GET("https://push2.eastmoney.com/api/qt/stock/fflow/kline/get")
    suspend fun getMarketFundFlow(
        @Query("secid") secId: String = "1.000001",
        @Query("fields1") fields1: String = "f1,f2,f3,f7",
        @Query("fields2") fields2: String = "f51,f52,f53,f54,f55,f56,f57,f58,f59,f60,f61,f62,f63,f64,f65",
        @Query("klt") klt: Int = 1,
        @Query("lmt") lmt: Int = 0,
        @Query("_") timestamp: Long = System.currentTimeMillis()
    ): FundFlowResponse

    // 北向资金 (与原项目 marketS2N.vue 一致)
    @GET("https://push2.eastmoney.com/api/qt/kamt.rtmin/get")
    suspend fun getNorthSouthFlow(
        @Query("fields1") fields1: String = "f1,f2,f3,f4",
        @Query("fields2") fields2: String = "f51,f52,f53,f54,f55,f56",
        @Query("_") timestamp: Long = System.currentTimeMillis()
    ): NorthSouthFlowResponse
}
