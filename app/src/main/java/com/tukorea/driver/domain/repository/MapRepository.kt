package com.tukorea.driver.domain.repository

import com.tukorea.driver.domain.util.MapResult
import com.tukorea.driver.domain.model.MapLocation
import com.tukorea.driver.domain.model.WebSocketConnectionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface MapRepository {
    /**
     * 현재 위치를 가져옵니다.
     * @return 성공 시 위치 정보, 실패 시 에러를 포함한 Result
     */
    suspend fun getCurrentLocation(): MapResult<MapLocation>

    /**
     * 위치 권한이 허용되었는지 확인합니다.
     * @return 위치 권한 허용 여부
     */
    fun isLocationPermissionGranted(): Boolean

    /**
     * 백그라운드에서 지속적으로 위치 업데이트를 받아옵니다.
     * @return 위치 업데이트를 제공하는 Flow
     */
    fun getLocationUpdates(): Flow<MapLocation>

    /**
     * 실시간 위치를 백엔드 서버로 전송합니다.
     * @param busId 버스/드라이버 ID
     * @param location 위치 정보
     * @param status 운행 상태 ("RUNNING", "WAITING", "STOP")
     */
    fun sendLocation(busId: String, location: MapLocation, status: String)

    /**
     * WebSocket 연결을 시작합니다.
     */
    fun connectWebSocket()

    /**
     * WebSocket 연결을 종료합니다.
     */
    fun disconnectWebSocket()

    /**
     * WebSocket 연결 상태를 관찰합니다.
     * @return WebSocket 연결 상태 Flow
     */
    fun observeWebSocketConnectionState(): StateFlow<WebSocketConnectionState>

    /**
     * 서버에서 특정 버스의 최신 위치를 조회합니다.
     * @param busId 버스/드라이버 ID
     * @return 성공 시 위치 정보 (null 가능), 실패 시 에러를 포함한 Result
     * @note null은 비즈니스 로직이므로 UseCase에서 처리합니다.
     */
    suspend fun getBusLocationFromServer(busId: String): MapResult<MapLocation?>
}
