package com.example.util

import com.example.data.SaleRecord
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

object Formatters {

    private val indianLocale = Locale.Builder().setLanguage("en").setRegion("IN").build()

    fun formatInr(amount: Double): String {
        val rounded = if (abs(amount - amount.toLong()) < 0.005) {
            amount.toLong().toDouble()
        } else {
            amount
        }
        val symbols = DecimalFormatSymbols(indianLocale)
        val hasDecimals = abs(rounded - rounded.toLong()) >= 0.005
        val pattern = if (hasDecimals) "#,##,##0.00" else "#,##,##0"
        val df = DecimalFormat(pattern, symbols)
        return "₹${df.format(rounded)}"
    }

    fun formatNumericPlain(amount: Double): String {
        return if (abs(amount - amount.toLong()) < 0.005) {
            amount.toLong().toString()
        } else {
            String.format(Locale.US, "%.2f", amount)
        }
    }

    fun formatDateTimeForSheet(date: Date = Date()): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(date)
    }

    fun currentMonthKey(date: Date = Date()): String {
        return SimpleDateFormat("yyyy-MM", Locale.US).format(date)
    }

    fun monthKeyToLabel(monthKey: String): String {
        if (monthKey == ALL_MONTHS_KEY) return "All Months"
        val parsed = runCatching {
            SimpleDateFormat("yyyy-MM", Locale.US).parse(monthKey)
        }.getOrNull() ?: return monthKey
        return SimpleDateFormat("MMMM yyyy", Locale.ENGLISH).format(parsed)
    }

    fun extractMonthKey(rawDate: String): String {
        val millis = parseDateToMillis(rawDate)
        if (millis != null) {
            return SimpleDateFormat("yyyy-MM", Locale.US).format(Date(millis))
        }
        val match = Regex("""(\d{4})-(\d{2})""").find(rawDate)
        if (match != null) return match.value
        return currentMonthKey()
    }

    fun formatDisplayDateTime(rawDate: String): String {
        val millis = parseDateToMillis(rawDate) ?: return rawDate.ifBlank { "—" }
        return SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH).format(Date(millis))
    }

    fun parseDateToMillis(rawDate: String): Long? {
        val trimmed = rawDate.trim()
        if (trimmed.isEmpty()) return null
        val patterns = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd HH:mm",
            "yyyy-MM-dd",
            "dd/MM/yyyy HH:mm:ss",
            "dd/MM/yyyy HH:mm",
            "dd/MM/yyyy",
            "dd-MM-yyyy HH:mm",
            "dd-MM-yyyy",
            "dd MMM yyyy, hh:mm a",
            "MMM dd, yyyy HH:mm"
        )
        for (pattern in patterns) {
            val parsed = runCatching {
                SimpleDateFormat(pattern, Locale.ENGLISH).apply { isLenient = false }.parse(trimmed)
            }.getOrNull()
            if (parsed != null) return parsed.time
        }
        return null
    }

    fun generateBillId(date: Date, sequence: Int): String {
        val stamp = SimpleDateFormat("yyyyMMdd", Locale.US).format(date)
        return "AA-$stamp-${sequence.toString().padStart(3, '0')}"
    }

    fun formatSplitBreakdown(sale: SaleRecord): String {
        val parts = mutableListOf<String>()
        if (sale.cashAmount > 0.005) parts.add("Cash ${formatInr(sale.cashAmount)}")
        if (sale.onlineAmount > 0.005) parts.add("Online ${formatInr(sale.onlineAmount)}")
        if (sale.cardAmount > 0.005) parts.add("Card ${formatInr(sale.cardAmount)}")
        return parts.joinToString(" + ")
    }

    const val ALL_MONTHS_KEY = "ALL"
}
