package com.fundhelper.app.data.api

import com.fundhelper.app.data.model.*
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface FundApi {
    @GET("FundMNewApi/FundMNFInfo")
    suspend fun getFundRealtimeData(@Query("Fcodes") codes: String, @Query("pageIndex") pageIndex: Int = 1, @Query("pageSize") pageSize: Int = 200, @Query("plat") plat: String = "Android", @Query("appType") appType: String = "ttjj", @Query("product") product: String = "EFund", @Query("Version") version: String = "1", @Query("deviceid") deviceId: String = "android_fund_helper"): FundListResponse

    @GET suspend fun searchFund(@Url url: String = "https://fundsuggest.eastmoney.com/FundSearch/api/FundSearchAPI.ashx", @Query("m") m: Int = 9, @Query("key") keyword: String, @Query("_") timestamp: Long = System.currentTimeMillis()): FundSearchResponse

    @GET("FundMApi/FundVarietieValuationDetail.ashx") suspend fun getFundTrend(@Query("FCODE") code: String, @Query("deviceid") deviceId: String = "Wap", @Query("plat") plat: String = "Wap", @Query("product") product: String = "EFund", @Query("version") version: String = "2.0.0", @Query("_") timestamp: Long = System.currentTimeMillis()): FundTrendResponse

    @GET("FundMApi/FundNetDiagram.ashx") suspend fun getFundNetDiagram(@Query("FCODE") code: String, @Query("RANGE") range: String = "y", @Query("deviceid") deviceId: String = "Wap", @Query("plat") plat: String = "Wap", @Query("product") product: String = "EFund", @Query("version") version: String = "2.0.0", @Query("_") timestamp: Long = System.currentTimeMillis()): FundNetDiagramResponse

    @GET("FundMApi/FundYieldDiagramNew.ashx") suspend fun getFundYieldDiagram(@Query("FCODE") code: String, @Query("RANGE") range: String = "y", @Query("deviceid") deviceId: String = "Wap", @Query("plat") plat: String = "Wap", @Query("product") product: String = "EFund", @Query("version") version: String = "2.0.0", @Query("_") timestamp: Long = System.currentTimeMillis()): FundYieldDiagramResponse

    @GET("FundMNewApi/FundMNHisNetList") suspend fun getFundHistoryNav(@Query("FCODE") code: String, @Query("pageIndex") pageIndex: Int = 1, @Query("pageSize") pageSize: Int = 30, @Query("deviceid") deviceId: String = "Wap", @Query("plat") plat: String = "Wap", @Query("product") product: String = "EFund", @Query("version") version: String = "2.0.0", @Query("_") timestamp: Long = System.currentTimeMillis()): FundHistoryNavResponse

    @GET("FundMNewApi/FundMNInverstPosition") suspend fun getFundPosition(@Query("FCODE") code: String, @Query("deviceid") deviceId: String = "Wap", @Query("plat") plat: String = "Wap", @Query("product") product: String = "EFund", @Query("version") version: String = "2.0.0", @Query("_") timestamp: Long = System.currentTimeMillis()): FundPositionResponse

    @GET("FundMApi/FundBaseTypeInformation.ashx") suspend fun getFundInfo(@Query("FCODE") code: String, @Query("deviceid") deviceId: String = "Wap", @Query("plat") plat: String = "Wap", @Query("product") product: String = "EFund", @Query("version") version: String = "2.0.0", @Query("_") timestamp: Long = System.currentTimeMillis()): FundInfoResponse

    @GET("FundMNewApi/FundMNInfoNew") suspend fun getFundManager(@Query("FCODE") code: String, @Query("deviceid") deviceId: String = "Wap", @Query("plat") plat: String = "Wap", @Query("product") product: String = "EFund", @Query("version") version: String = "2.0.0", @Query("_") timestamp: Long = System.currentTimeMillis()): FundManagerResponse

    @GET suspend fun getIndexQuote(@Url url: String = "https://push2.eastmoney.com/api/qt/ulist.np/get", @Query("fltt") fltt: Int = 2, @Query("fields") fields: String = "f2,f3,f4,f6,f7,f8,f12,f13,f14,f15,f16,f17,f18,f20,f21", @Query("secids") secIds: String, @Query("_") timestamp: Long = System.currentTimeMillis()): IndexQuoteResponse

    @GET("https://push2.eastmoney.com/api/qt/clist/get") suspend fun getSectors(@Query("pn") pn: Int = 1, @Query("pz") pz: Int = 500, @Query("po") po: Int = 1, @Query("np") np: Int = 1, @Query("fields") fields: String = "f2,f3,f8,f9,f12,f14,f62,f184", @Query("fs") fs: String = "m:90+t:2", @Query("fid") fid: String = "f62", @Query("_") timestamp: Long = System.currentTimeMillis()): SectorResponse

    @GET("https://push2.eastmoney.com/api/qt/stock/fflow/kline/get") suspend fun getMarketFundFlow(@Query("secid") secId: String = "1.000001", @Query("fields1") fields1: String = "f1,f2,f3,f7", @Query("fields2") fields2: String = "f51,f52,f53,f54,f55,f56,f57,f58,f59,f60,f61,f62,f63,f64,f65", @Query("klt") klt: Int = 1, @Query("lmt") lmt: Int = 0, @Query("_") timestamp: Long = System.currentTimeMillis()): FundFlowResponse

    @GET("https://push2his.eastmoney.com/api/qt/stock/fflow/daykline/get") suspend fun getMarketFundFlowDay(@Query("secid") secId: String = "1.000001", @Query("fields1") fields1: String = "f1,f2,f3,f7", @Query("fields2") fields2: String = "f51,f52,f53,f54,f55,f56,f57,f58,f59,f60,f61,f62,f63,f64,f65", @Query("klt") klt: Int = 101, @Query("lmt") lmt: Int = 10, @Query("ut") ut: String = "b2884a393a59ad64002292a3e90d46a5", @Query("_") timestamp: Long = System.currentTimeMillis()): FundFlowResponse

    @GET("https://push2.eastmoney.com/api/qt/kamt.rtmin/get") suspend fun getNorthSouthFlow(@Query("fields1") fields1: String = "f1,f2,f3,f4", @Query("fields2") fields2: String = "f51,f52,f53,f54,f55,f56", @Query("_") timestamp: Long = System.currentTimeMillis()): NorthSouthFlowResponse

    @GET("https://datacenter-web.eastmoney.com/dataapi/bkzj/getbkzj") suspend fun getBkzj(@Query("key") key: String = "f62", @Query("code") code: String = "m:90+t:2", @Query("_") timestamp: Long = System.currentTimeMillis()): BkzjResponse

    @GET
    suspend fun getFundGz(@Url url: String): ResponseBody

    @GET("https://push2his.eastmoney.com/api/qt/stock/kline/get")
    suspend fun getIndexKline(@Query("secid") secId: String, @Query("fields1") fields1: String = "f1,f2,f3,f4,f5,f6", @Query("fields2") fields2: String = "f51,f52,f53,f54,f55,f56,f57,f58,f59,f60,f61", @Query("klt") klt: Int = 101, @Query("lmt") lmt: Int = 120, @Query("fqt") fqt: Int = 1, @Query("ut") ut: String = "b2884a393a59ad64002292a3e90d46a5", @Query("_") timestamp: Long = System.currentTimeMillis()): IndexKlineResponse
}
