package com.tukorea.driver.ui.navigation

/**
 * 앱 내 네비게이션 화면을 정의하는 sealed class
 */
sealed class Screen(val route: String) {
    data object Home : Screen("home")

    data object Map : Screen("map")

    data object Temp : Screen("temp")
}
