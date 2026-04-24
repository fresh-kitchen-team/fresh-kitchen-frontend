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
                    // Reselect behavior:
                    // If the user taps the already-selected AI Chat tab, return to the "base" AI chat state
                    // (close overlays and scroll to the latest message) without clearing message history.
                    if (destination == BottomNavRoute.Scan) {
                        // Scan must always start from the initial state.
                        // This clears any in-progress/finished scan flow (including scan_result) and recreates scan.
                        navController.navigate(BottomNavRoute.Scan.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = true
                                saveState = false
                            }
                            launchSingleTop = true
                            restoreState = false
                        }
                    } else if (selected && destination == BottomNavRoute.AiChat) {
                        navController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set("ai_chat_reselect", System.currentTimeMillis())
                    } else if (!selected) {
                        navController.navigate(destination.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
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
