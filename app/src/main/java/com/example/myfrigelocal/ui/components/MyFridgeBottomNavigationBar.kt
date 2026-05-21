package com.example.myfrigelocal.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.DocumentScanner
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import com.example.myfrigelocal.navigation.BottomNavRoute
import com.example.myfrigelocal.ui.theme.BottomNavBarBackground
import com.example.myfrigelocal.ui.theme.BottomNavSelected
import com.example.myfrigelocal.ui.theme.BottomNavUnselected

@Composable
fun MyFridgeBottomNavigationBar(
    navController: NavHostController,
    destinations: List<BottomNavRoute>,
    modifier: Modifier = Modifier,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(
        modifier = modifier,
        containerColor = BottomNavBarBackground,
        tonalElevation = 0.dp,
    ) {
        destinations.forEach { destination ->
            val selected =
                currentDestination
                    ?.hierarchy
                    ?.any { it.route == destination.route }
                    ?: false
            NavigationBarItem(
                selected = selected,
                onClick = {
                    when {
                        // Scan: 항상 초기 상태로. 스캔 결과/진행중 화면을 모두 폐기하고
                        //       start destination(home)까지 inclusive popup 후 새로 시작.
                        destination == BottomNavRoute.Scan -> {
                            navController.navigate(BottomNavRoute.Scan.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    inclusive = true
                                    saveState = false
                                }
                                launchSingleTop = true
                                restoreState = false
                            }
                        }
                        // AI Chat reselect: 이미 AI 채팅 탭이 선택된 상태에서 다시 누르면
                        //                  "최신 메시지로 스크롤 / 오버레이 닫기" 신호만 보낸다.
                        //                  AI Chat ViewModel 은 Activity scope 이라 메시지 히스토리는 유지.
                        selected && destination == BottomNavRoute.AiChat -> {
                            navController.currentBackStackEntry
                                ?.savedStateHandle
                                ?.set("ai_chat_reselect", System.currentTimeMillis())
                        }
                        // 모든 탭(Home / AiChat / AnalyticsTips) 클릭 시
                        // 어떤 sub 화면(consumption_detail, disposal_guide, inventory_list, profile,
                        // settings, search, storage_tip_* 등) 에 있든 항상 그 탭의 초기 화면으로 이동.
                        // saveState=false / restoreState=false 로 이전에 떠있던 sub 화면을 폐기.
                        !selected -> {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = false
                                }
                                launchSingleTop = true
                                restoreState = false
                            }
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = destination.icon(),
                        contentDescription = stringResource(destination.labelRes),
                    )
                },
                label = { Text(text = stringResource(destination.labelRes)) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = BottomNavSelected,
                    selectedTextColor = BottomNavSelected,
                    indicatorColor = Color.Transparent,
                    unselectedIconColor = BottomNavUnselected,
                    unselectedTextColor = BottomNavUnselected,
                ),
            )
        }
    }
}

private fun BottomNavRoute.icon(): ImageVector = when (this) {
    BottomNavRoute.Home -> Icons.Outlined.Home
    BottomNavRoute.Scan -> Icons.Outlined.DocumentScanner
    BottomNavRoute.AiChat -> Icons.Outlined.SmartToy
    BottomNavRoute.AnalyticsTips -> Icons.Outlined.BarChart
}
