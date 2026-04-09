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

@Composable
fun MainScaffold(
    mainScaffoldViewModel: MainScaffoldViewModel = viewModel(),
) {
    val navController = rememberNavController()
    val destinations = mainScaffoldViewModel.bottomNavDestinations()

    Scaffold(
        bottomBar = {
            MyFridgeBottomNavigationBar(
                navController = navController,
                destinations = destinations,
            )
        },
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
        )
    }
}
