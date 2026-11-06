package com.tukorea.driver.ui.map

import com.tukorea.driver.domain.model.MapLocation
import com.tukorea.driver.domain.model.RouteProgress
import com.tukorea.driver.domain.model.ShuttleRoute
import com.tukorea.driver.domain.model.WebSocketConnectionState

/**
 * 맵 화면의 UI 상태를 관리하는 데이터 클래스
 * @param isLoading 위치 로딩 중 여부
 * @param currentLocation 현재 위치 정보
 * @param isLocationPermissionGranted 위치 권한 허용 여부
 * @param webSocketConnectionState WebSocket 연결 상태
 * @param error 에러 메시지 (에러 발생 시)
 */
data class MapUiState(
    val isLoading: Boolean = false,
    val currentLocation: MapLocation? = null,
    val isLocationPermissionGranted: Boolean = false,
    val webSocketConnectionState: WebSocketConnectionState = WebSocketConnectionState.DISCONNECTED,
    val error: String? = null,
    /** 고른 노선의 경로(지도에 선으로 그린다). */
    val route: ShuttleRoute? = null,
    /** 고른 노선 위에서 다음 정류장과 남은 거리. 위치나 노선이 없으면 null. */
    val progress: RouteProgress? = null,
)
