package com.gosuraksha.app.ui.history.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.gosuraksha.app.design.tokens.ColorTokens
import com.gosuraksha.app.history.model.HistoryItem
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

enum class RiskLevel { SAFE, WARNING, THREAT, UNKNOWN }

fun String.toRiskLevel(): RiskLevel {
    return when (this.trim().uppercase()) {
        "SAFE", "CLEAN", "LOW", "OK", "CLEAR", "BENIGN" -> RiskLevel.SAFE
        "MEDIUM", "MODERATE", "WARNING", "WARN", "SUSPICIOUS" -> RiskLevel.WARNING
        "HIGH", "CRITICAL", "THREAT", "MALICIOUS", "DANGEROUS", "PHISHING" -> RiskLevel.THREAT
        else -> RiskLevel.UNKNOWN
    }
}

enum class ScanType(val label: String, val emoji: String) {
    QR("QR Code", "⬛"),
    URL("URL Scan", "🔗"),
    FILE("File Scan", "📄"),
    APK("APK Scan", "📦"),
    NETWORK("Network", "📶"),
    UNKNOWN("Scan", "🔍")
}

fun inferScanType(inputText: String): ScanType {
    val lower = inputText.trim().lowercase()
    return when {
        lower.startsWith("upi://") || lower.contains("pa=") -> ScanType.QR
        lower.endsWith(".apk") -> ScanType.APK
        lower.endsWith(".pdf") || lower.endsWith(".doc") ||
            lower.endsWith(".docx") || lower.endsWith(".xls") ||
            lower.endsWith(".xlsx") || lower.endsWith(".zip") ||
            lower.endsWith(".exe") -> ScanType.FILE
        lower.startsWith("http://") || lower.startsWith("https://") ||
            lower.startsWith("www.") -> ScanType.URL
        lower.contains(".") && !lower.contains(" ") -> ScanType.URL
        else -> ScanType.UNKNOWN
    }
}

sealed class HistoryListItem {
    data class Header(val label: String) : HistoryListItem()
    data class Entry(val item: HistoryItem) : HistoryListItem()
}

fun List<HistoryItem>.toGroupedListItems(): List<HistoryListItem> {
    val today = LocalDate.now()
    val zone = ZoneId.systemDefault()
    val formatter = DateTimeFormatter.ofPattern("d MMMM")

    return this
        .sortedByDescending { it.created_at }
        .groupBy { item ->
            try {
                Instant.parse(item.created_at).atZone(zone).toLocalDate()
            } catch (_: Exception) {
                today
            }
        }
        .flatMap { (date, items) ->
            val label = when {
                date == today -> "Today · ${today.format(formatter)}"
                date == today.minusDays(1) -> "Yesterday · ${date.format(formatter)}"
                ChronoUnit.DAYS.between(date, today) < 7 -> {
                    date.format(DateTimeFormatter.ofPattern("EEEE · d MMMM"))
                }
                else -> date.format(DateTimeFormatter.ofPattern("d MMMM yyyy"))
            }
            listOf(HistoryListItem.Header(label)) + items.map { HistoryListItem.Entry(it) }
        }
}

fun String.toFormattedTime(): String {
    return try {
        val instant = Instant.parse(this)
        val zone = ZoneId.systemDefault()
        val date = instant.atZone(zone).toLocalDate()
        val today = LocalDate.now()
        val minutesAgo = ChronoUnit.MINUTES.between(instant, Instant.now())
        val hoursAgo = ChronoUnit.HOURS.between(instant, Instant.now())

        when {
            date == today && minutesAgo < 2 -> "Just now"
            date == today && minutesAgo < 60 -> "$minutesAgo min ago"
            date == today && hoursAgo < 24 -> "$hoursAgo hr ago"
            date == today.minusDays(1) -> {
                "Yesterday · ${instant.atZone(zone).format(DateTimeFormatter.ofPattern("h:mm a"))}"
            }
            else -> instant.atZone(zone).format(DateTimeFormatter.ofPattern("d MMM · h:mm a"))
        }
    } catch (_: Exception) {
        this
    }
}

@Composable
fun RiskLevel.containerColor(): Color = when (this) {
    RiskLevel.SAFE -> ColorTokens.success().copy(alpha = 0.12f)
    RiskLevel.WARNING -> ColorTokens.warning().copy(alpha = 0.12f)
    RiskLevel.THREAT -> ColorTokens.error().copy(alpha = 0.12f)
    RiskLevel.UNKNOWN -> ColorTokens.surface()
}

@Composable
fun RiskLevel.contentColor(): Color = when (this) {
    RiskLevel.SAFE -> ColorTokens.success()
    RiskLevel.WARNING -> ColorTokens.warning()
    RiskLevel.THREAT -> ColorTokens.error()
    RiskLevel.UNKNOWN -> ColorTokens.textSecondary()
}

@Composable
fun RiskLevel.label(): String = when (this) {
    RiskLevel.SAFE -> "Safe"
    RiskLevel.WARNING -> "Warning"
    RiskLevel.THREAT -> "Threat"
    RiskLevel.UNKNOWN -> "Unknown"
}

fun RiskLevel.scoreBarFraction(score: Int): Float = (score / 100f).coerceIn(0f, 1f)
