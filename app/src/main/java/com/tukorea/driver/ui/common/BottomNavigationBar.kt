package com.tukorea.driver.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tukorea.driver.R
import com.tukorea.driver.ui.navigation.Screen
import com.tukorea.driver.ui.theme.BusTheme
import com.tukorea.driver.ui.theme.busColors

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
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
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
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                        unselectedIconColor = MaterialTheme.busColors.textTertiary,
                        unselectedTextColor = MaterialTheme.busColors.textTertiary,
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
