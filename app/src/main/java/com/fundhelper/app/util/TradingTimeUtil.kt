package com.fundhelper.app.util

import com.fundhelper.app.data.model.TradingStatus
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * 交易时间判断工具 (与原项目 background.js isDuringDate 一致)
 *
 * 关键逻辑:
 * - 交易时间: 9:30-11:35, 13:00-15:05 (收盘后5分钟缓冲)
 * - 节假日休市
 * - 周末休市 (但补班日开市)
 */
object TradingTimeUtil {

    // 节假日 (holiday: true)
    private val holidays = setOf(
        // 2025
        "2025-01-01", "2025-01-28", "2025-01-29", "2025-01-30", "2025-01-31",
        "2025-02-01", "2025-02-02", "2025-02-03", "2025-02-04",
        "2025-04-04", "2025-04-05", "2025-04-06",
        "2025-05-01", "2025-05-02", "2025-05-03", "2025-05-04", "2025-05-05",
        "2025-05-31", "2025-06-01", "2025-06-02",
        "2025-10-01", "2025-10-02", "2025-10-03", "2025-10-04",
        "2025-10-05", "2025-10-06", "2025-10-07",
        // 2026
        "2026-01-01", "2026-01-02",
        "2026-02-17", "2026-02-18", "2026-02-19", "2026-02-20",
        "2026-02-21", "2026-02-22", "2026-02-23",
        "2026-04-05", "2026-04-06", "2026-04-07",
        "2026-05-01", "2026-05-02", "2026-05-03",
        "2026-06-19", "2026-06-20", "2026-06-21",
        "2026-10-01", "2026-10-02", "2026-10-03",
        "2026-10-04", "2026-10-05", "2026-10-06", "2026-10-07"
    )

    // 补班日 (weekends that are trading days)
    private val makeupWorkDays = setOf(
        "2025-01-26", "2025-02-08",  // 春节补班
        "2025-04-27",                 // 劳动节补班
        "2025-09-28",                 // 国庆补班
        "2025-10-11",                 // 国庆补班
        "2026-02-15", "2026-02-28",  // 春节补班
        "2026-04-26",                 // 劳动节补班
        "2026-10-10"                  // 国庆补班
    )

    fun getTradingStatus(): TradingStatus {
        val now = LocalDate.now(ZoneId.of("Asia/Shanghai"))
        val time = LocalTime.now(ZoneId.of("Asia/Shanghai"))
        val dayOfWeek = now.dayOfWeek
        val dateStr = now.toString()

        // 节假日休市
        if (holidays.contains(dateStr)) {
            return TradingStatus.HOLIDAY
        }

        // 周末休市 (但补班日开市)
        val isWeekend = dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY
        if (isWeekend && !makeupWorkDays.contains(dateStr)) {
            return TradingStatus.CLOSED
        }

        // 交易时间判断 (与原项目一致: 9:30-11:35, 13:00-15:05)
        val amOpen = LocalTime.of(9, 30)
        val amClose = LocalTime.of(11, 35)
        val pmOpen = LocalTime.of(13, 0)
        val pmClose = LocalTime.of(15, 5)

        return when {
            time.isBefore(amOpen) -> TradingStatus.NOT_STARTED
            time in amOpen..amClose -> TradingStatus.TRADING
            time.isAfter(amClose) && time.isBefore(pmOpen) -> TradingStatus.LUNCH_BREAK
            time in pmOpen..pmClose -> TradingStatus.TRADING
            else -> TradingStatus.CLOSED
        }
    }

    fun isTradingTime(): Boolean = getTradingStatus() == TradingStatus.TRADING

    fun getStatusText(status: TradingStatus): String = when (status) {
        TradingStatus.NOT_STARTED -> "未开盘"
        TradingStatus.TRADING -> "交易中"
        TradingStatus.LUNCH_BREAK -> "午间休市"
        TradingStatus.CLOSED -> "已收盘"
        TradingStatus.HOLIDAY -> "休市(假日)"
    }
}
