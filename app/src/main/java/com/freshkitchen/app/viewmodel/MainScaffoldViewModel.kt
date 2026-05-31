package com.freshkitchen.app.viewmodel

import androidx.lifecycle.ViewModel
import com.freshkitchen.app.navigation.BottomNavRoute

/**
 * Host-level logic for the main shell (tabs, future global state).
 * Tab selection is driven by [androidx.navigation.NavController]; this VM supplies config and helpers.
 */
class MainScaffoldViewModel : ViewModel() {

    fun bottomNavDestinations(): List<BottomNavRoute> = BottomNavRoute.entries
}
