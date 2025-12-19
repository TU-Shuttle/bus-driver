package com.tukorea.driver.ui.map

import com.tukorea.driver.domain.model.MapLocation

/**
 * 맵 화면의 UI 상태를 관리하는 데이터 클래스
 * @param isLoading 위치 로딩 중 여부
 * @param currentLocation 현재 위치 정보
 * @param isLocationPermissionGranted 위치 권한 허용 여부
 * @param error 에러 메시지 (에러 발생 시)
 */
data class MapUiState(
    val isLoading: Boolean = false,
    val currentLocation: MapLocation? = null,
    val isLocationPermissionGranted: Boolean = false,
    val error: String? = null
)

