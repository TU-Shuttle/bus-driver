package com.tukorea.driver.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.tukorea.driver.R
import com.tukorea.driver.ui.navigation.Screen
import com.tukorea.driver.ui.theme.BusTheme
import com.tukorea.driver.ui.theme.Gray400
import com.tukorea.driver.ui.theme.PrimaryBlue
import com.tukorea.driver.ui.theme.White

@Composable
fun BottomNavigationBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onHomeToggle: (() -> Unit)? = null,
) {
    val navItems =
        listOf(
            BottomNavItem(Screen.Home.route, Icons.Default.Home, stringResource(R.string.driver_nav_home)),
            BottomNavItem(Screen.Map.route, Icons.Default.Map, stringResource(R.string.driver_nav_map)),
        )

    NavigationBar(
        containerColor = White,
    ) {
        navItems.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                    )
                },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    if (item.route == Screen.Home.route && currentRoute == Screen.Home.route) {
                        onHomeToggle?.invoke()
                    } else {
                        onNavigate(item.route)
                    }
                },
                colors =
                    NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryBlue,
                        selectedTextColor = PrimaryBlue,
                        indicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                        unselectedIconColor = Gray400,
                        unselectedTextColor = Gray400,
                    ),
            )
        }
    }
}

@Preview(showBackground = true, name = "Bottom Navigation Bar - Home Selected")
@Composable
private fun BottomNavigationBarHomePreview() {
    BusTheme {
        BottomNavigationBar(
            currentRoute = Screen.Home.route,
            onNavigate = {},
        )
    }
}
