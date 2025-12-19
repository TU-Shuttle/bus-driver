package com.tukorea.driver.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.tukorea.driver.ui.map.MapScreen
import com.tukorea.driver.ui.temp.TempScreen

@Composable
fun BusNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Map.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Map.route) {
            MapScreen()
        }
        composable(Screen.Temp.route) {
            TempScreen()
        }
    }
}
