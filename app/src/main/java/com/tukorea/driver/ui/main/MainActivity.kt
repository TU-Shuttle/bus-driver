package com.tukorea.driver.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.tukorea.driver.ui.home.DriverHomeRoute
import com.tukorea.driver.ui.theme.BusTheme
import dagger.hilt.android.AndroidEntryPoint

/** 기사 앱은 화면 하나(지도 + 운행 시트)라 탭 네비게이션을 두지 않는다. */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BusTheme {
                DriverHomeRoute()
            }
        }
    }
}
