package com.example.myfrigelocal.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.myfrigelocal.navigation.AppNavHost
import com.example.myfrigelocal.ui.components.MyFridgeBottomNavigationBar
import com.example.myfrigelocal.viewmodel.MainScaffoldViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.runtime.getValue

@Composable
fun MainScaffold(
    mainScaffoldViewModel: MainScaffoldViewModel = viewModel(),
) {
    val navController = rememberNavController()
    val destinations = mainScaffoldViewModel.bottomNavDestinations()

    // 현재 라우트 확인
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            // 온보딩일 때는 하단바 숨김
            val noNavBarRoutes = setOf("onboarding", "login", "onboarding_setup", "profile")
            if (currentRoute !in noNavBarRoutes) {
                MyFridgeBottomNavigationBar(
                    navController = navController,
                    destinations = destinations,
                )
            }
        },
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
        )
    }
}