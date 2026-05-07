package com.example.myfrigelocal.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.myfrigelocal.ui.screens.AiChatScreen
import com.example.myfrigelocal.ui.screens.AnalyticsTipsScreen
import com.example.myfrigelocal.ui.screens.ConsumptionDetailScreen
import com.example.myfrigelocal.ui.screens.HomeScreen
import com.example.myfrigelocal.ui.screens.ScanScreen
import com.example.myfrigelocal.ui.screens.ScanResultScreen
import com.example.myfrigelocal.navigation.ScanNav
import com.example.myfrigelocal.ui.screens.InventoryListScreen
import com.example.myfrigelocal.ui.screens.DisposalGuideScreen
import com.example.myfrigelocal.ui.screens.StorageTipCategoryScreen
import com.example.myfrigelocal.ui.screens.StorageTipDetailScreen
import com.example.myfrigelocal.ui.screens.OnboardingScreen
import com.example.myfrigelocal.ui.screens.OnboardingSetupScreen
import com.example.myfrigelocal.ui.screens.LoginScreen
import com.example.myfrigelocal.ui.screens.ProfileScreen
import com.example.myfrigelocal.ui.screens.SettingsScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = "onboarding",
        modifier = modifier,
    ) {
        // 온보딩 서비스 소개 (앱 첫 실행 시 시작점)
        composable("onboarding") {
            OnboardingScreen(
                onFinish = {
                    navController.navigate("login") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }

        // 로그인 화면
        composable("login") {
            LoginScreen(
                onNewUser = {
                    // 신규 회원 → 기본 정보 입력 설정 화면
                    navController.navigate("onboarding_setup")
                },
                onExistingUser = {
                    // 기존 회원 → 바로 홈으로
                    navController.navigate(BottomNavRoute.Home.route) {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onBackClick = {
                    // 서비스 소개로 뒤로가기
                    navController.popBackStack()
                }
            )
        }

        // 온보딩 설정 (알러지 / 선호 음식 스타일 / 식재료 간편 등록)
        composable("onboarding_setup") {
            OnboardingSetupScreen(
                onFinish = {
                    navController.navigate(BottomNavRoute.Home.route) {
                        popUpTo("onboarding_setup") { inclusive = true }
                    }
                },
                onBackToLogin = {
                    // 로그인 화면으로 뒤로가기
                    navController.popBackStack()
                }
            )
        }

        composable(BottomNavRoute.Home.route) {
            HomeScreen(
                onNavigateToInventory = { filter ->
                    navController.navigate("inventory_list/$filter")
                },
                onNavigateToProfile = {
                    navController.navigate("profile")
                },
                onNavigateToSettings = {
                    navController.navigate("settings")
                }
            )
        }

        // 프로필 화면
        composable("profile") {
            ProfileScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // 설정 화면
        composable("settings") {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
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
        composable(BottomNavRoute.AnalyticsTips.route) { AnalyticsTipsScreen(navController = navController) }

        // Not a bottom-tab destination. Reached after a successful scan.
        composable(ScanNav.routeResult) { ScanResultScreen(navController = navController) }

        composable("inventory_list/{filter}") {
            InventoryListScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("consumption_detail") {
            ConsumptionDetailScreen(navController = navController)
        }

        composable("disposal_guide") {
            DisposalGuideScreen(navController = navController)
        }

        composable("storage_tip_detail") {
            StorageTipDetailScreen(navController = navController)
        }

        composable(
            route = "storage_tip_category/{type}",
            arguments = listOf(navArgument("type") { type = NavType.StringType }),
        ) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type").orEmpty()
            StorageTipCategoryScreen(
                navController = navController,
                type = type,
            )
        }
    }
}