package com.tukorea.driver.data.location

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * LocationForegroundService를 시작/중지하는 매니저
 */
@Singleton
class LocationServiceManager
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private companion object {
            private const val TAG = "LocationServiceManager"
        }

        /**
         * Foreground Service를 시작합니다.
         * 운행/대기 중일 때 호출됩니다.
         */
        fun startService() {
            Log.d(TAG, "Foreground Service 시작")
            val intent = Intent(context, LocationForegroundService::class.java)
            ContextCompat.startForegroundService(context, intent)
        }

        /**
         * Foreground Service를 중지합니다.
         * 운행 종료 시 호출됩니다.
         */
        fun stopService() {
            Log.d(TAG, "Foreground Service 중지")
            val intent = Intent(context, LocationForegroundService::class.java)
            context.stopService(intent)
        }
    }
