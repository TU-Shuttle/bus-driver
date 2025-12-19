package com.tukorea.driver.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.tukorea.driver.domain.error.MapError
import com.tukorea.driver.domain.model.MapLocation
import com.tukorea.driver.domain.util.MapResult
import com.tukorea.driver.domain.util.Result
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class LocationDataSource @Inject constructor(
    private val context: Context
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
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
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

            @SuppressLint("MissingPermission") val location: Location? = withTimeout(10000) {
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationTokenSource.token
                ).await() // Task를 코루틴 suspend 함수로 변환
            }

            // Location 객체를 MapLocation 모델로 변환
            location?.let {
                Log.d(
                    TAG,
                    "위치 가져오기 성공!!!!!!!: lat=${it.latitude}, lng=${it.longitude}, accuracy=${it.accuracy}"
                )
                Result.Success(
                    MapLocation(
                        latitude = it.latitude, longitude = it.longitude, accuracy = it.accuracy
                    )
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
        } catch (e: Exception) {
            Log.e(TAG, "위치 가져오기 실패", e)
            Result.Error(MapError.Unknown(e.message))
        }
    }

    companion object {
        // logcat 필터링 태그
        private const val TAG = "LocationDataSource"
    }
}

