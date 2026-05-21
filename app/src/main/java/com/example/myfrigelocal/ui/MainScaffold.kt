package com.example.myfrigelocal.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myfrigelocal.navigation.AppNavHost
import com.example.myfrigelocal.navigation.ScanNav
import com.example.myfrigelocal.ui.components.MyFridgeBottomNavigationBar
import com.example.myfrigelocal.ui.theme.FreshGreenDark
import com.example.myfrigelocal.viewmodel.MainScaffoldViewModel

@Composable
fun MainScaffold(
    mainScaffoldViewModel: MainScaffoldViewModel = viewModel(),
    isLoggedIn: Boolean = false,
) {
    val navController = rememberNavController()
    val destinations = mainScaffoldViewModel.bottomNavDestinations()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // 하단 바 / FAB 숨길 화면
    val noNavBarRoutes = setOf(
        "onboarding",
        "login",
        "onboarding_setup",
        "profile",
        "settings",
        "search",
        "manual_add",
        ScanNav.routeResult,
    )

    val showBottomBar = currentRoute !in noNavBarRoutes

    // FAB는 홈 / 인벤토리 리스트에서만 표시
    val showFab = currentRoute == "home" ||
            currentRoute?.startsWith("inventory_list") == true

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                MyFridgeBottomNavigationBar(
                    navController = navController,
                    destinations = destinations,
                )
            }
        },
        floatingActionButton = {
            if (showFab) {
                FloatingActionButton(
                    onClick = { navController.navigate("manual_add") },
                    shape = CircleShape,
                    containerColor = FreshGreenDark,
                    contentColor = Color.White,
                ) {
                    Icon(Icons.Default.Add, contentDescription = "식재료 수동 추가")
                }
            }
        },
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            isLoggedIn = isLoggedIn,
        )
    }
}
