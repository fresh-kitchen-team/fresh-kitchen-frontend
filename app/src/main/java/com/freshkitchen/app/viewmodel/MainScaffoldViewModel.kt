package com.freshkitchen.app.viewmodel

import androidx.lifecycle.ViewModel
import com.freshkitchen.app.navigation.BottomNavRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Host-level logic for the main shell (tabs, future global state).
 * Tab selection is driven by [androidx.navigation.NavController]; this VM supplies config and helpers.
 */
@HiltViewModel
class MainScaffoldViewModel @Inject constructor() : ViewModel() {

    fun bottomNavDestinations(): List<BottomNavRoute> = BottomNavRoute.entries
}
