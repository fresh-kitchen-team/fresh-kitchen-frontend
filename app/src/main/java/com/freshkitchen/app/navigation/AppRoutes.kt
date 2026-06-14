package com.freshkitchen.app.navigation

/** Non–bottom-tab routes shared across NavHost and MainScaffold. */
object AppRoutes {
    const val onboarding = "onboarding"
    const val login = "login"
    const val onboardingSetup = "onboarding_setup"
    const val search = "search"
    const val profile = "profile"
    const val settings = "settings"
    const val manualAdd = "manual_add"
    const val inventoryList = "inventory_list/{filter}"
    const val consumptionDetail = "consumption_detail"
    const val disposalGuide = "disposal_guide"
    const val storageTipDetail = "storage_tip_detail"
    const val storageTipCategory = "storage_tip_category/{type}"

    /** Routes where the bottom navigation bar and FAB should be hidden. */
    val noBottomBarRoutes: Set<String> = setOf(
        onboarding,
        login,
        onboardingSetup,
        profile,
        settings,
        search,
        manualAdd,
        ScanNav.routeResult,
        consumptionDetail,
        disposalGuide,
        storageTipDetail,
    )

    fun hidesBottomBar(route: String?): Boolean {
        if (route == null) return false
        if (route in noBottomBarRoutes) return true
        return route.startsWith("storage_tip_category/")
    }
}
