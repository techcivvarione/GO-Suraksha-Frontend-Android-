package com.gosuraksha.app.ui.news

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.ui.graphics.Color
import com.gosuraksha.app.news.model.NewsItem

val AuroraViolet = Color(0xFF8B5CF6)
val AuroraBlue = Color(0xFF3B82F6)
val AuroraPurpleDim = Color(0x338B5CF6)
val AuroraBlueDim = Color(0x333B82F6)
val GlassDark = Color(0x0DFFFFFF)
val GlassBorder = Color(0x1FFFFFFF)
val GlassBorderLight = Color(0xCCFFFFFF)
val GlassLight = Color(0x99FFFFFF)

enum class NewsCategory(val apiValue: String) {
    ALL("ALL"),
    AI("AI"),
    CYBER("CYBER"),
    TECH("TECH")
}

fun filterNewsByCategory(news: List<NewsItem>, selectedCategory: NewsCategory): List<NewsItem> {
    return news.filter {
        selectedCategory == NewsCategory.ALL ||
            it.category.equals(selectedCategory.apiValue, ignoreCase = true)
    }
}

fun articleIdFor(item: NewsItem): String {
    return item.link ?: "${item.source}|${item.title}|${item.published_at}"
}

fun triggerHapticTick(context: Context) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(VibratorManager::class.java)
            vm?.defaultVibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(18, 60))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(18)
            }
        }
    } catch (_: Exception) {
    }
}
