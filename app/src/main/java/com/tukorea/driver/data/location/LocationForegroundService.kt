package com.tukorea.driver.data.location

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.tukorea.driver.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

@AndroidEntryPoint
class LocationForegroundService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null

    // 위치 업데이트를 외부로 전달하기 위한 SharedFlow
    private val _locationFlow = MutableSharedFlow<com.tukorea.driver.domain.model.MapLocation>(replay = 1)
    val locationFlow: SharedFlow<com.tukorea.driver.domain.model.MapLocation> = _locationFlow.asSharedFlow()

    companion object {
        @Volatile
        private var instance: LocationForegroundService? = null

        fun getInstance(): LocationForegroundService? = instance

        private const val TAG = "LocationForegroundService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "location_service_channel"
        private const val LOCATION_UPDATE_INTERVAL_MS = 2_000L
        private const val LOCATION_FASTEST_INTERVAL_MS = 2_000L
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service 생성")
        instance = this
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service 시작")
        startForeground(NOTIFICATION_ID, createNotification())
        startLocationUpdates()
        return START_STICKY // 서비스가 종료되어도 재시작
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service 종료")
        stopLocationUpdates()
        instance = null
    }

    /**
     * Notification Channel 생성 (Android 8.0+)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "위치 추적 서비스",
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "버스 위치를 서버로 전송하기 위한 위치 추적 서비스"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Foreground Service용 Notification 생성
     */
    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("버스 위치 추적 중")
            .setContentText("위치를 서버로 전송하고 있습니다")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    /**
     * 위치 업데이트 시작
     */
    private fun startLocationUpdates() {
        val hasPermission = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            Log.w(TAG, "위치 권한이 없어 위치 업데이트를 시작할 수 없습니다.")
            stopSelf()
            return
        }

        val locationRequest =
            LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, LOCATION_UPDATE_INTERVAL_MS)
                .setMinUpdateIntervalMillis(LOCATION_FASTEST_INTERVAL_MS)
                .setMaxUpdateDelayMillis(LOCATION_UPDATE_INTERVAL_MS)
                .build()

        locationCallback =
            object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult.lastLocation?.let { location ->
                        val mapLocation =
                            com.tukorea.driver.domain.model.MapLocation(
                                latitude = location.latitude,
                                longitude = location.longitude,
                                accuracy = location.accuracy,
                            )
                        Log.d(
                            TAG,
                            "위치 업데이트 (Foreground Service): lat=${mapLocation.latitude}, lng=${mapLocation.longitude}",
                        )
                        _locationFlow.tryEmit(mapLocation)
                    }
                }
            }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.getMainLooper(),
            )
            Log.d(TAG, "위치 업데이트 시작 성공")
        } catch (e: SecurityException) {
            Log.e(TAG, "위치 권한 오류", e)
            stopSelf()
        }
    }

    /**
     * 위치 업데이트 중지
     */
    private fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
            locationCallback = null
            Log.d(TAG, "위치 업데이트 중지")
        }
    }
}
