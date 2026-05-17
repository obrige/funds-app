package com.fundhelper.app.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// 板块资金流 API: datacenter-web.eastmoney.com/dataapi/bkzj/getbkzj
@JsonClass(generateAdapter = true)
data class BkzjResponse(
    val rc: Int = 0,
    val data: BkzjData?
)

@JsonClass(generateAdapter = true)
data class BkzjData(
    val total: Int = 0,
    val diff: List<BkzjItem>?
)

@JsonClass(generateAdapter = true)
data class BkzjItem(
    @Json(name = "f12") val code: String?,
    @Json(name = "f14") val name: String?,
    @Json(name = "f62") val mainInflow: Double?,
    @Json(name = "f164") val mainOutflow: Double?
)

// 板块类型
enum class BkType(val label: String, val codeParam: String) {
    INDUSTRY("行业", "m:90+t:2"),
    CONCEPT("概念", "m:90+t:3"),
    REGION("地域", "m:90+t:1")
}

// 时间周期
enum class BkPeriod(val label: String, val statCode: String) {
    TODAY("今日", "s:4"),
    DAY5("5日", "s:5"),
    DAY10("10日", "s:6")
}
