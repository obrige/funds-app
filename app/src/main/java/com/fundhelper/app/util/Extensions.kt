package com.fundhelper.app.util

import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

fun Double.formatGain(): String {
    val df = DecimalFormat("+#,##0.00;-#,##0.00")
    return df.format(this)
}

fun Double.formatPercent(): String {
    val sign = if (this >= 0) "+" else ""
    return "$sign${String.format(Locale.US, "%.2f", this)}%"
}

fun Double.formatAmount(): String {
    return when {
        kotlin.math.abs(this) >= 1_0000_0000 -> String.format("%.2f亿", this / 1_0000_0000)
        kotlin.math.abs(this) >= 1_0000 -> String.format("%.2f万", this / 1_0000)
        else -> String.format("%.2f", this)
    }
}

fun Double.formatCurrency(): String {
    val nf = NumberFormat.getCurrencyInstance(Locale.CHINA)
    return nf.format(this)
}

fun Double.toColorHex(): String {
    val r = if (this >= 0) 0xF5 else 0x4E
    val g = if (this >= 0) 0x6C else 0xB6
    val b = if (this >= 0) 0x6C else 0x1B
    return String.format("#FF%02X%02X%02X", r, g, b)
}
