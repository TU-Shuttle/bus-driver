package com.tukorea.driver.ui.map

import android.app.Activity
import android.content.Context
import android.graphics.PointF
import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.PathOverlay
import com.naver.maps.map.util.FusedLocationSource
import com.tukorea.driver.R
import com.tukorea.driver.domain.model.MapLocation
import com.tukorea.driver.domain.model.ShuttleRoute
import com.tukorea.driver.domain.model.ShuttleStop
import com.tukorea.driver.domain.util.LocationUtils

private const val CAMERA_ANIMATION_DURATION_MS = 500L
private const val CENTER = 0.5f
private const val STOP_CAPTION_SP = 14f

@Composable
fun NaverMapView(
    modifier: Modifier = Modifier,
    currentLocation: MapLocation?,
    isLocationPermissionGranted: Boolean,
    onMapReady: () -> Unit = {},
    bottomPadding: Int = 0,
    onMapInitialized: ((NaverMap) -> Unit)? = null,
    route: ShuttleRoute? = null,
    stopCaption: @Composable (ShuttleStop) -> String = { "" },
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

    // 고른 노선: 학생 앱과 같은 굵은 단색 선 + 정류장 마커
    val density = LocalDensity.current
    val routeLine = remember { PathOverlay() }
    val stopMarkers = remember { mutableListOf<Marker>() }
    val originIcon = remember { context.overlay(R.drawable.ic_map_stop_origin) }
    val stopIcon = remember { context.overlay(R.drawable.ic_map_stop_destination) }
    val captions = route?.direction?.stops.orEmpty().associateWith { stopCaption(it) }
    LaunchedEffect(naverMap, route, isDarkTheme) {
        val map = naverMap ?: return@LaunchedEffect
        stopMarkers.forEach { it.map = null }
        stopMarkers.clear()
        val points = route?.points.orEmpty()
        if (route == null || points.size < 2) {
            routeLine.map = null
            return@LaunchedEffect
        }
        val ink = (if (isDarkTheme) Color.White else Color.Black).toArgb()
        val halo = (if (isDarkTheme) Color.Black else Color.White).toArgb()
        routeLine.apply {
            coords = points.map { LatLng(it.latitude, it.longitude) }
            width = with(density) { 5.dp.roundToPx() }
            outlineWidth = with(density) { 1.5.dp.roundToPx() }
            color = ink
            outlineColor = halo
            this.map = map
        }
        route.direction.stops.forEachIndexed { index, stop ->
            stopMarkers +=
                Marker().apply {
                    position = LatLng(stop.point.latitude, stop.point.longitude)
                    icon = if (index == 0) originIcon else stopIcon
                    anchor = PointF(CENTER, CENTER)
                    captionText = captions[stop].orEmpty()
                    captionTextSize = STOP_CAPTION_SP
                    captionColor = ink
                    captionHaloColor = halo
                    isHideCollidedSymbols = true
                    this.map = map
                }
        }
    }

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
                    // 기기 언어와 상관없이 한글 지명만 보여준다.
                    map.locale = java.util.Locale.KOREA

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
                // 시트에 가려지는 만큼 지도 중심을 위로 올린다.
                map.setContentPadding(0, 0, 0, bottomPadding)
            }
        },
        modifier = modifier,
    )
}

private fun Context.overlay(
    @DrawableRes id: Int,
): OverlayImage = OverlayImage.fromBitmap(requireNotNull(ContextCompat.getDrawable(this, id)).toBitmap())
