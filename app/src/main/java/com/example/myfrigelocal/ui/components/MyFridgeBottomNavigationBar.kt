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
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        modifier = modifier,
        containerColor = BottomNavBarBackground,
        tonalElevation = 0.dp,
    ) {
        destinations.forEach { destination ->
            val selected = currentRoute == destination.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(destination.route) {
                        popUpTo(BottomNavRoute.start.route) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
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
