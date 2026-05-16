package com.fundhelper.app.util

object Constants {
    val DEFAULT_INDICES = listOf(
        Triple("1.000001", "上证指数", "000001"),
        Triple("0.399001", "深证成指", "399001"),
        Triple("0.399006", "创业板指", "399006")
    )

    val AVAILABLE_INDICES = listOf(
        Triple("1.000001", "上证指数", "000001"),
        Triple("0.399001", "深证成指", "399001"),
        Triple("0.399006", "创业板指", "399006"),
        Triple("1.000300", "沪深300", "000300"),
        Triple("1.000016", "上证50", "000016"),
        Triple("0.399673", "创业板50", "399673"),
        Triple("1.000905", "中证500", "000905"),
        Triple("1.000852", "中证1000", "000852"),
        Triple("100.HSI", "恒生指数", "HSI"),
        Triple("100.HSCEI", "国企指数", "HSCEI"),
        Triple("105.IXIC", "纳斯达克", "IXIC"),
        Triple("105.DJIA", "道琼斯", "DJIA"),
        Triple("105.SPX", "标普500", "SPX")
    )

    const val REFRESH_INTERVAL_WORKER = "fund_refresh_worker"
    const val PREF_DARK_MODE = "dark_mode"
    const val PREF_SHOW_BADGE = "show_badge"
    const val PREF_BADGE_CONTENT = "badge_content"
    const val PREF_BADGE_TYPE = "badge_type"
    const val PREF_SHOW_GSZ = "show_gsz"
    const val PREF_SHOW_AMOUNT = "show_amount"
    const val PREF_SHOW_GAINS = "show_gains"
    const val PREF_SHOW_COST = "show_cost"
    const val PREF_SHOW_COST_RATE = "show_cost_rate"
    const val PREF_REFRESH_INTERVAL = "refresh_interval"
}
