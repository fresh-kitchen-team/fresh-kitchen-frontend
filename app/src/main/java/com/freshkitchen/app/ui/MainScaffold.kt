package com.freshkitchen.app.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.freshkitchen.app.navigation.AppNavHost
import com.freshkitchen.app.navigation.ScanNav
import com.freshkitchen.app.ui.components.MyFridgeBottomNavigationBar
import com.freshkitchen.app.ui.theme.FreshGreenDark
import com.freshkitchen.app.viewmodel.MainScaffoldViewModel

@Composable
fun MainScaffold(
    mainScaffoldViewModel: MainScaffoldViewModel = viewModel(),
    isLoggedIn: Boolean = false,
) {
    val navController = rememberNavController()
    val destinations = mainScaffoldViewModel.bottomNavDestinations()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // 인벤토리 선택 모드 상태를 MainScaffold 레벨에서 관리
    var isInventorySelectMode by remember { mutableStateOf(false) }

    // 화면 이동 시 선택 모드 초기화
    androidx.compose.runtime.LaunchedEffect(currentRoute) {
        if (currentRoute?.startsWith("inventory_list") != true) {
            isInventorySelectMode = false
        }
    }

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

    // FAB는 홈 / 인벤토리 리스트에서만 표시, 선택 모드일 때는 숨김
    val showFab = (currentRoute == "home" ||
            currentRoute?.startsWith("inventory_list") == true) &&
            !isInventorySelectMode

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
        val layoutDirection = LocalLayoutDirection.current
        // 스캔 결과: 앱 하단 탭은 숨기고, 시스템 내비게이션 바 inset은 ScanResultBottomBar에서 처리
        val contentPadding =
            if (currentRoute == ScanNav.routeResult) {
                PaddingValues(
                    top = innerPadding.calculateTopPadding(),
                    start = innerPadding.calculateStartPadding(layoutDirection),
                    end = innerPadding.calculateEndPadding(layoutDirection),
                    bottom = 0.dp,
                )
            } else {
                innerPadding
            }
        AppNavHost(
            navController = navController,
            modifier = Modifier.padding(contentPadding),
            isLoggedIn = isLoggedIn,
            onInventorySelectModeChange = { isInventorySelectMode = it },
        )
    }
}
