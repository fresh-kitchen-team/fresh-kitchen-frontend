package com.freshkitchen.app.navigation

import androidx.annotation.StringRes
import com.freshkitchen.app.R

enum class BottomNavRoute(
    val route: String,
    @param:StringRes val labelRes: Int,
) {
    Home("home", R.string.nav_home),
    Scan("scan", R.string.nav_scan),
    AiChat("ai_chat", R.string.nav_ai_chat),
    AnalyticsTips("analytics_tips", R.string.nav_analytics_tips),
    ;

    companion object {
        val start = Home

        fun fromRoute(route: String?): BottomNavRoute? =
            entries.firstOrNull { it.route == route }
    }
}
