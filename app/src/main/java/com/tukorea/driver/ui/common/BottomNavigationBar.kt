package com.tukorea.driver.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.tukorea.driver.ui.navigation.Screen
import com.tukorea.driver.ui.theme.BusTheme
import com.tukorea.driver.ui.theme.PrimaryBlue
import com.tukorea.driver.ui.theme.Gray400
import com.tukorea.driver.ui.theme.White

data class BottomNavItem(
    val route: String, val icon: ImageVector, val label: String
)

@Composable
fun BottomNavigationBar(
    currentRoute: String?, onNavigate: (String) -> Unit, onHomeToggle: (() -> Unit)? = null
) {
    val navItems = listOf(
        BottomNavItem(Screen.Home.route, Icons.Default.Home, "홈"),
        BottomNavItem(Screen.Calendar.route, Icons.Default.DateRange, "예약"),
        BottomNavItem(Screen.QuickRide.route, Icons.Default.LocalFireDepartment, "바로탑승"),
        BottomNavItem(Screen.Settings.route, Icons.Default.Settings, "설정")
    )

    NavigationBar(
        containerColor = White
    ) {
        navItems.forEach { item ->
            NavigationBarItem(
                icon = {
                Icon(
                    imageVector = item.icon, contentDescription = item.label
                )
            }, label = { Text(item.label) }, selected = currentRoute == item.route, onClick = {
                if (item.route == Screen.Home.route && currentRoute == Screen.Home.route) {
                    onHomeToggle?.invoke()
                } else {
                    onNavigate(item.route)
                }
            }, colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryBlue, selectedTextColor = PrimaryBlue,

                indicatorColor = androidx.compose.ui.graphics.Color.Transparent,

                unselectedIconColor = Gray400, unselectedTextColor = Gray400
            )
            )
        }
    }
}

@Preview(showBackground = true, name = "Bottom Navigation Bar - Home Selected")
@Composable
private fun BottomNavigationBarHomePreview() {
    BusTheme {
        BottomNavigationBar(
            currentRoute = Screen.Home.route, onNavigate = {})
    }
}
