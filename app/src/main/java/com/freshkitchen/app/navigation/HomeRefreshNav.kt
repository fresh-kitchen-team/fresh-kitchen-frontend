package com.freshkitchen.app.navigation

import androidx.navigation.NavController

/**
 * 홈 요약 재조회 트리거를 [ScanNav.keyRefreshHome] SavedStateHandle에 기록한다.
 *
 * Scan 탭 진입 시 back stack에서 home이 제거될 수 있어 [NavController.getBackStackEntry]가
 * 실패할 수 있다. 이 경우 크래시 없이 no-op 한다 (다음 home 진입 시 ViewModel init으로 로드).
 */
fun NavController.requestHomeRefresh(refreshAt: Long = System.currentTimeMillis()) {
    val homeEntry = runCatching { getBackStackEntry(BottomNavRoute.Home.route) }.getOrNull()
        ?: return
    homeEntry.savedStateHandle.set(ScanNav.keyRefreshHome, refreshAt)
}
