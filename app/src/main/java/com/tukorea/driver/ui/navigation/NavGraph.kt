package com.tukorea.driver.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.tukorea.driver.ui.home.HomeScreen
import com.tukorea.driver.ui.map.MapScreen
import com.tukorea.driver.ui.temp.TempScreen

@Composable
fun BusNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Home.route,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateTo = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        }
        composable(Screen.Map.route) {
            MapScreen()
        }
        composable(Screen.Temp.route) {
            TempScreen()
        }
    }
}
