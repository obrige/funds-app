package com.fundhelper.app.util

import com.fundhelper.app.data.model.TradingStatus
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

object TradingTimeUtil {

    private val holidays2024 = setOf(
        "2024-01-01", "2024-02-10", "2024-02-11", "2024-02-12", "2024-02-13",
        "2024-02-14", "2024-02-15", "2024-02-16", "2024-02-17",
        "2024-04-04", "2024-04-05", "2024-04-06",
        "2024-05-01", "2024-05-02", "2024-05-03", "2024-05-04", "2024-05-05",
        "2024-06-08", "2024-06-09", "2024-06-10",
        "2024-09-15", "2024-09-16", "2024-09-17",
        "2024-10-01", "2024-10-02", "2024-10-03", "2024-10-04",
        "2024-10-05", "2024-10-06", "2024-10-07"
    )

    private val holidays2025 = setOf(
        "2025-01-01", "2025-01-28", "2025-01-29", "2025-01-30", "2025-01-31",
        "2025-02-01", "2025-02-02", "2025-02-03", "2025-02-04",
        "2025-04-04", "2025-04-05", "2025-04-06",
        "2025-05-01", "2025-05-02", "2025-05-03", "2025-05-04", "2025-05-05",
        "2025-05-31", "2025-06-01", "2025-06-02",
        "2025-10-01", "2025-10-02", "2025-10-03", "2025-10-04",
        "2025-10-05", "2025-10-06", "2025-10-07"
    )

    private val holidays2026 = setOf(
        "2026-01-01", "2026-01-02",
        "2026-02-17", "2026-02-18", "2026-02-19", "2026-02-20",
        "2026-02-21", "2026-02-22", "2026-02-23",
        "2026-04-05", "2026-04-06", "2026-04-07",
        "2026-05-01", "2026-05-02", "2026-05-03",
        "2026-06-19", "2026-06-20", "2026-06-21",
        "2026-10-01", "2026-10-02", "2026-10-03",
        "2026-10-04", "2026-10-05", "2026-10-06", "2026-10-07"
    )

    private val allHolidays = holidays2024 + holidays2025 + holidays2026

    fun getTradingStatus(): TradingStatus {
        val now = LocalDate.now(ZoneId.of("Asia/Shanghai"))
        val time = LocalTime.now(ZoneId.of("Asia/Shanghai"))
        val dayOfWeek = now.dayOfWeek

        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            return TradingStatus.CLOSED
        }

        if (allHolidays.contains(now.toString())) {
            return TradingStatus.HOLIDAY
        }

        val amOpen = LocalTime.of(9, 30)
        val amClose = LocalTime.of(11, 30)
        val pmOpen = LocalTime.of(13, 0)
        val pmClose = LocalTime.of(15, 0)

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
