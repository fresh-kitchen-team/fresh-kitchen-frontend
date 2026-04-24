package com.example.myfrigelocal.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavBackStackEntry
import com.example.myfrigelocal.ui.screens.AiChatScreen
import com.example.myfrigelocal.ui.screens.AnalyticsTipsScreen
import com.example.myfrigelocal.ui.screens.ConsumptionDetailScreen
import com.example.myfrigelocal.ui.screens.HomeScreen
import com.example.myfrigelocal.ui.screens.ScanScreen
import com.example.myfrigelocal.ui.screens.ScanResultScreen
import com.example.myfrigelocal.navigation.ScanNav
import com.example.myfrigelocal.ui.screens.InventoryListScreen
import com.example.myfrigelocal.ui.screens.StorageTipDetailScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = BottomNavRoute.start.route,
        modifier = modifier,
    ) {
        composable(BottomNavRoute.Home.route) {
            HomeScreen(
                onNavigateToInventory = { filter ->
                    navController.navigate("inventory_list/$filter")
                }
            )
        }
        composable(BottomNavRoute.Scan.route) { backStackEntry ->
            ScanScreen(
                navController = navController,
                lifecycleOwner = backStackEntry,
            )
        }
        composable(BottomNavRoute.AiChat.route) { backStackEntry ->
            AiChatScreen(backStackEntry = backStackEntry)
        }
        composable(BottomNavRoute.AnalyticsTips.route) { AnalyticsTipsScreen() }

        // Not a bottom-tab destination. Reached after a successful scan.
        composable(ScanNav.routeResult) { ScanResultScreen(navController = navController) }

        composable("inventory_list/{filter}") {
            InventoryListScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(BottomNavRoute.AnalyticsTips.route) { AnalyticsTipsScreen(navController = navController) }

        composable("consumption_detail") {
            ConsumptionDetailScreen(navController = navController)
        }

        composable("storage_tip_detail") {
            StorageTipDetailScreen(navController = navController)
        }
    }
}
