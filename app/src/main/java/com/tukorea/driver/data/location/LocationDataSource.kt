package com.tukorea.driver.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.tukorea.driver.domain.error.MapError
import com.tukorea.driver.domain.model.MapLocation
import com.tukorea.driver.domain.util.MapResult
import com.tukorea.driver.domain.util.Result
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton

private const val LOCATION_TIMEOUT_MS = 10_000L
private const val LOCATION_UPDATE_INTERVAL_MS = 2_000L
private const val LOCATION_FASTEST_INTERVAL_MS = 2_000L

@Singleton
class LocationDataSource
    @Inject
    constructor(
        private val context: Context,
    ) {
        /**
         * FusedLocationProviderClient 인스턴스
         * GPS, Wi-Fi, 셀룰러 네트워크를 결합하여 가장 정확한 위치를 제공합니다.
         */
        private val fusedLocationClient: FusedLocationProviderClient by lazy {
            LocationServices.getFusedLocationProviderClient(context)
        }

        /**
         * 위치 권한이 허용되었는지 확인합니다.
         * FINE_LOCATION 또는 COARSE_LOCATION 중 하나라도 허용되면 true를 반환합니다.
         * @return 위치 권한 허용 여부
         */
        fun isLocationPermissionGranted(): Boolean {
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED
        }

        /**
         * 현재 위치를 가져옵니다.
         * Google Play Services의 FusedLocationProviderClient를 사용하여
         * 높은 정확도로 위치를 가져옵니다.
         *
         * @return 성공 시 위치 정보, 실패 시 에러를 포함한 Result
         */
        suspend fun getCurrentLocation(): MapResult<MapLocation> {
            if (!isLocationPermissionGranted()) {
                Log.w(TAG, "위치 권한이 없습니다.")
                return Result.Error(MapError.PermissionDenied)
            }

            return try {
                // 취소 토큰 생성 (필요시 위치 요청 취소 가능)
                val cancellationTokenSource = CancellationTokenSource()

                @SuppressLint("MissingPermission")
                val location: Location? =
                    withTimeout(LOCATION_TIMEOUT_MS) {
                        fusedLocationClient.getCurrentLocation(
                            Priority.PRIORITY_HIGH_ACCURACY,
                            cancellationTokenSource.token,
                        ).await() // Task를 코루틴 suspend 함수로 변환
                    }

                // Location 객체를 MapLocation 모델로 변환
                location?.let {
                    Log.d(
                        TAG,
                        "위치 가져오기 성공!!!!!!!: lat=${it.latitude}, lng=${it.longitude}, accuracy=${it.accuracy}",
                    )
                    Result.Success(
                        MapLocation(
                            latitude = it.latitude,
                            longitude = it.longitude,
                            accuracy = it.accuracy,
                        ),
                    )
                } ?: run {
                    Log.w(TAG, "위치 정보가 null입니다.")
                    Result.Error(MapError.LocationNotFound)
                }
            } catch (e: SecurityException) {
                Log.e(TAG, "위치 권한이 거부", e)
                Result.Error(MapError.PermissionDenied)
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "위치 가져오기 타임아웃", e)
                Result.Error(MapError.Timeout)
            } catch (e: IllegalStateException) {
                Log.e(TAG, "위치 서비스 상태 오류", e)
                Result.Error(MapError.Unknown(e.message))
            } catch (e: Exception) {
                // 예상치 못한 예외를 처리하기 위해 Exception을 사용
                // 로깅은 이미 수행됨
                Log.e(TAG, "위치 가져오기 실패", e)
                Result.Error(MapError.Unknown(e.message))
            }
        }

        /**
         * 백그라운드에서 지속적으로 위치 업데이트를 받아옵니다.
         * Flow를 통해 위치 변경사항을 스트림으로 제공합니다.
         *
         * @return 위치 업데이트를 제공하는 Flow
         */
        fun getLocationUpdates(): Flow<MapLocation> =
            callbackFlow {
                if (!isLocationPermissionGranted()) {
                    Log.w(TAG, "위치 권한이 없어 위치 업데이트를 시작할 수 없습니다.")
                    close()
                    return@callbackFlow
                }

                val locationRequest =
                    LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, LOCATION_UPDATE_INTERVAL_MS)
                        .setMinUpdateIntervalMillis(LOCATION_FASTEST_INTERVAL_MS)
                        .setMaxUpdateDelayMillis(LOCATION_UPDATE_INTERVAL_MS)
                        .build()

                val locationCallback =
                    object : LocationCallback() {
                        override fun onLocationResult(locationResult: LocationResult) {
                            locationResult.lastLocation?.let { location ->
                                val mapLocation =
                                    MapLocation(
                                        latitude = location.latitude,
                                        longitude = location.longitude,
                                        accuracy = location.accuracy,
                                    )
                                Log.d(
                                    TAG,
                                    "위치 업데이트: lat=${mapLocation.latitude}, lng=${mapLocation.longitude}, " +
                                        "accuracy=${mapLocation.accuracy}",
                                )
                                trySend(mapLocation)
                            }
                        }
                    }

                @SuppressLint("MissingPermission")
                val task =
                    fusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        Looper.getMainLooper(),
                    )

                task.addOnFailureListener { exception ->
                    Log.e(TAG, "위치 업데이트 요청 실패", exception)
                    close(exception)
                }

                awaitClose {
                    Log.d(TAG, "위치 업데이트 중지")
                    fusedLocationClient.removeLocationUpdates(locationCallback)
                }
            }

        companion object {
            private const val TAG = "LocationDataSource"
        }
    }
