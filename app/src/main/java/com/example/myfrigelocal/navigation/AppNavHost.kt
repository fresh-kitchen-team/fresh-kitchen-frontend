package com.example.myfrigelocal.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.myfrigelocal.ui.screens.AiChatScreen
import com.example.myfrigelocal.ui.screens.AnalyticsTipsScreen
import com.example.myfrigelocal.ui.screens.HomeScreen
import com.example.myfrigelocal.ui.screens.ScanScreen

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
        composable(BottomNavRoute.Home.route) { HomeScreen() }
        composable(BottomNavRoute.Scan.route) { ScanScreen() }
        composable(BottomNavRoute.AiChat.route) { AiChatScreen() }
        composable(BottomNavRoute.AnalyticsTips.route) { AnalyticsTipsScreen() }
    }
}
