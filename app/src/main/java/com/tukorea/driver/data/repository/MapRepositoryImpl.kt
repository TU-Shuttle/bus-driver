package com.tukorea.driver.data.repository

import android.util.Log
import com.tukorea.driver.data.api.ApiService
import com.tukorea.driver.data.location.LocationDataSource
import com.tukorea.driver.data.location.LocationForegroundService
import com.tukorea.driver.data.util.NetworkErrorHandler
import com.tukorea.driver.data.websocket.WebSocketDataSource
import com.tukorea.driver.domain.error.MapError
import com.tukorea.driver.domain.model.MapLocation
import com.tukorea.driver.domain.repository.MapRepository
import com.tukorea.driver.domain.util.MapResult
import com.tukorea.driver.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.merge
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MapRepositoryImpl
    @Inject
    constructor(
        private val locationDataSource: LocationDataSource,
        private val webSocketDataSource: WebSocketDataSource,
        private val apiService: ApiService,
    ) : MapRepository {
        private companion object {
            private const val TAG = "MapRepositoryImpl"
        }
        /**
         * LocationDataSource를 통해 현재 위치를 가져옵니다.
         */
        override suspend fun getCurrentLocation(): MapResult<MapLocation> {
            return locationDataSource.getCurrentLocation()
        }

        /**
         * LocationDataSource를 통해 위치 권한 상태를 확인합니다.
         */
        override fun isLocationPermissionGranted(): Boolean {
            return locationDataSource.isLocationPermissionGranted()
        }

        /**
         * LocationDataSource를 통해 위치 업데이트 Flow를 제공합니다.
         * Foreground Service가 실행 중이면 Service의 위치도 병합합니다.
         */
        override fun getLocationUpdates(): Flow<MapLocation> {
            val foregroundService = LocationForegroundService.getInstance()
            return if (foregroundService != null) {
                // Foreground Service가 실행 중이면 Service의 위치 Flow와 병합
                merge(
                    locationDataSource.getLocationUpdates(),
                    foregroundService.locationFlow,
                )
            } else {
                // Foreground Service가 없으면 기존 방식 사용
                locationDataSource.getLocationUpdates()
            }
        }

        /**
         * WebSocketDataSource를 통해 위치를 서버로 전송합니다.
         */
        override fun sendLocation(busId: String, location: MapLocation, status: String, direction: String?) {
            webSocketDataSource.sendLocation(busId, location, status, direction)
        }

        /**
         * WebSocketDataSource를 통해 WebSocket 연결을 시작합니다.
         */
        override fun connectWebSocket() {
            webSocketDataSource.connect()
        }

        /**
         * WebSocketDataSource를 통해 WebSocket 연결을 종료합니다.
         */
        override fun disconnectWebSocket() {
            webSocketDataSource.disconnect()
        }

        /**
         * WebSocketDataSource를 통해 연결 상태를 관찰합니다.
         */
        override fun observeWebSocketConnectionState() = webSocketDataSource.connectionState

        /**
         * ApiService를 통해 서버에서 버스 위치를 조회합니다.
         * Repository는 데이터 소스 추상화만 담당하며, 비즈니스 로직은 UseCase에서 처리합니다.
         */
        override suspend fun getBusLocationFromServer(busId: String): MapResult<MapLocation?> {
            return NetworkErrorHandler.handleMapNetworkRequest(
                tag = TAG,
                block = {
                    val response = apiService.getBusLocation(busId)

                    if (response == null) {
                        Log.d(TAG, "서버 응답: null (busId=$busId)")
                        Result.Success(null)
                    } else {
                        Log.d(
                            TAG,
                            "서버에서 버스 위치 조회 성공: busId=$busId, lat=${response.lat}, lng=${response.lng}",
                        )

                        Result.Success(
                            MapLocation(
                                latitude = response.lat,
                                longitude = response.lng,
                                accuracy = response.accuracy,
                            ),
                        )
                    }
                },
            )
        }
    }
