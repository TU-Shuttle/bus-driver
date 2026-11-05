package com.tukorea.driver.ui.map

import android.Manifest
import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.util.FusedLocationSource
import com.tukorea.driver.domain.model.MapLocation
import com.tukorea.driver.domain.util.LocationUtils

private const val CAMERA_ANIMATION_DURATION_MS = 500L
private const val NAVIGATION_BAR_PADDING_DP = 80

@Composable
fun NaverMapView(
    modifier: Modifier = Modifier,
    currentLocation: MapLocation?,
    isLocationPermissionGranted: Boolean,
    onMapReady: () -> Unit = {},
    bottomPadding: Int = 0,
    onMapInitialized: ((NaverMap) -> Unit)? = null,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val isDarkTheme = isSystemInDarkTheme()

    // MapView 인스턴스 (Configuration change에도 유지)
    val mapView = remember { MapView(context) }

    // FusedLocationSource (네이버맵 SDK 위치 소스)
    // Note: requestCode는 Accompanist가 권한을 처리하므로 사용되지 않음
    val locationSource =
        remember {
            FusedLocationSource(context as Activity, 0)
        }

    // NaverMap 인스턴스
    var naverMap by remember { mutableStateOf<NaverMap?>(null) }

    // 마지막 업데이트 위치 (중복 업데이트 방지)
    var lastUpdatedLocation by remember { mutableStateOf<MapLocation?>(null) }

    // 맵이 준비되었는지 여부
    var isMapReady by remember { mutableStateOf(false) }

    // 권한 상태 변경 시 LocationTrackingMode 업데이트
    LaunchedEffect(isLocationPermissionGranted, naverMap) {
        naverMap?.let { map ->
            if (isLocationPermissionGranted) {
                map.locationTrackingMode = LocationTrackingMode.Follow
            } else {
                map.locationTrackingMode = LocationTrackingMode.None
            }
        }
    }

    // 현재 위치 변경 시 카메라 이동
    LaunchedEffect(currentLocation, naverMap) {
        currentLocation?.let { location ->
            naverMap?.let { map ->
                val shouldUpdate =
                    lastUpdatedLocation?.let { last ->
                        LocationUtils.isDistanceGreaterThan(
                            from = last,
                            to = location,
                            thresholdMeters = 2.0,
                        )
                    } ?: true

                if (shouldUpdate) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    val cameraUpdate =
                        CameraUpdate.scrollTo(latLng)
                            .animate(CameraAnimation.Easing, CAMERA_ANIMATION_DURATION_MS)
                    map.moveCamera(cameraUpdate)
                    lastUpdatedLocation = location
                }
            }
        }
    }

    LaunchedEffect(naverMap, isMapReady) {
        naverMap?.let { map ->
            if (isMapReady) {
                onMapInitialized?.invoke(map)
            }
        }
    }

    // 맵 준비 완료 콜백
    LaunchedEffect(isMapReady) {
        if (isMapReady) {
            onMapReady()
        }
    }

    // 라이프사이클 관리
    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_CREATE -> mapView.onCreate(Bundle())
                    Lifecycle.Event.ON_START -> mapView.onStart()
                    Lifecycle.Event.ON_RESUME -> mapView.onResume()
                    Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                    Lifecycle.Event.ON_STOP -> mapView.onStop()
                    Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                    else -> {}
                }
            }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDestroy()
        }
    }

    // LowMemory 처리
    DisposableEffect(Unit) {
        onDispose {
            mapView.onLowMemory()
        }
    }

    AndroidView(
        factory = {
            mapView.apply {
                onCreate(Bundle())
                getMapAsync { map ->
                    naverMap = map

                    // 위치 소스 설정 (FusedLocationSource)
                    map.locationSource = locationSource

                    // UI 설정
                    map.uiSettings.apply {
                        // 기본 컨트롤은 숨기고(우버처럼 깔끔하게) 위치 이동은 화면의 둥근 버튼이 맡는다.
                        isLocationButtonEnabled = false
                        isZoomControlEnabled = false
                        isCompassEnabled = false
                        isScaleBarEnabled = false
                    }

                    // 위치 추적 모드 설정
                    if (isLocationPermissionGranted) {
                        map.locationTrackingMode = LocationTrackingMode.Follow
                    }

                    isMapReady = true
                }
            }
        },
        update = { view ->
            // 시스템 바 영역만큼 맵 UI 컨트롤 위치 조정
            naverMap?.let { map ->
                // 다크 모드에서는 야간 지도로 전환
                map.mapType = if (isDarkTheme) NaverMap.MapType.Navi else NaverMap.MapType.Basic
                map.isNightModeEnabled = isDarkTheme
                val density = view.context.resources.displayMetrics.density
                val bottomPadding = (NAVIGATION_BAR_PADDING_DP * density).toInt() // 네비게이션 바 + 여유 공간
                map.setContentPadding(0, 0, 0, bottomPadding)
            }
        },
        modifier = modifier,
    )
}
