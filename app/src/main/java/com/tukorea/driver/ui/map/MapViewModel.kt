package com.tukorea.driver.ui.map

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tukorea.driver.domain.usecase.ConnectWebSocketUseCase
import com.tukorea.driver.domain.usecase.DisconnectWebSocketUseCase
import com.tukorea.driver.domain.usecase.GetBusIdUseCase
import com.tukorea.driver.domain.usecase.GetCurrentLocationUseCase
import com.tukorea.driver.domain.usecase.GetLocationUpdatesUseCase
import com.tukorea.driver.domain.util.Result
import com.tukorea.driver.domain.usecase.ObserveWebSocketConnectionStateUseCase
import com.tukorea.driver.domain.model.DrivingStatus
import com.tukorea.driver.domain.usecase.GetDrivingStatusUseCase
import com.tukorea.driver.domain.usecase.SendLocationUseCase
import com.tukorea.driver.ui.common.ErrorMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 지도 화면의 현재 위치 상태를 관리하는 ViewModel.
 *
 * - 위치 권한 여부
 * - 현재 단말 위치
 * - 로딩/에러 상태
 */
@HiltViewModel
class MapViewModel
    @Inject
    constructor(
        private val getCurrentLocationUseCase: GetCurrentLocationUseCase,
        private val getLocationUpdatesUseCase: GetLocationUpdatesUseCase,
        private val sendLocationUseCase: SendLocationUseCase,
        private val connectWebSocketUseCase: ConnectWebSocketUseCase,
        private val disconnectWebSocketUseCase: DisconnectWebSocketUseCase,
        private val observeWebSocketConnectionStateUseCase: ObserveWebSocketConnectionStateUseCase,
        private val getDrivingStatusUseCase: GetDrivingStatusUseCase,
        private val getBusIdUseCase: GetBusIdUseCase,
        private val errorMapper: ErrorMapper,
    ) : ViewModel() {
        private val _state = MutableStateFlow(MapUiState())
        val state: StateFlow<MapUiState> = _state

        private companion object {
            private const val TAG = "MapViewModel"
        }

        init {
            Log.d(TAG, "초기화: 위치 권한을 확인합니다.")
            checkLocationPermission()
            connectWebSocket()
            observeWebSocketState()
            startLocationUpdates()
            observeDrivingStatusChanges()
        }

        override fun onCleared() {
            super.onCleared()
            Log.d(TAG, "ViewModel 종료: WebSocket 연결 종료")
            disconnectWebSocketUseCase()
        }

        /**
         * 위치 권한이 허용되었는지 확인합니다.
         * - 권한 여부만 조회하며, 실제 위치 조회는 수행하지 않습니다.
         */
        fun checkLocationPermission() {
            val isGranted = getCurrentLocationUseCase.isLocationPermissionGranted()
            Log.d(TAG, "위치 권한 여부 확인: 허용 여부=$isGranted")
            _state.value = _state.value.copy(isLocationPermissionGranted = isGranted)
        }

        /**
         * 현재 위치를 로드합니다.
         * UseCase를 통해 위치 정보를 가져와 상태를 업데이트합니다.
         */
        fun loadCurrentLocation() {
            Log.d(TAG, "현재 위치 불러오기 시작")
            viewModelScope.launch {
                _state.value = _state.value.copy(isLoading = true, error = null)

                when (val result = getCurrentLocationUseCase()) {
                    is Result.Success -> {
                        Log.d(
                            TAG,
                            "현재 위치 불러오기 성공: 위도=${result.data.latitude}, 경도=${result.data.longitude}",
                        )
                        _state.value =
                            _state.value.copy(
                                isLoading = false,
                                currentLocation = result.data,
                                error = null,
                            )
                    }

                    is Result.Error -> {
                        val message = errorMapper.run { result.error.toUserMessage() }
                        Log.w(TAG, "현재 위치 불러오기 실패: 메시지=$message, 원본오류=${result.error}")
                        _state.value =
                            _state.value.copy(
                                isLoading = false,
                                error = message,
                            )
                    }
                }
            }
        }

        /**
         * 위치 권한이 허용되었을 때 호출됩니다.
         * 권한 상태를 업데이트하고 현재 위치를 로드합니다.
         */
        fun onLocationPermissionGranted() {
            Log.d(TAG, "위치 권한이 허용되었습니다.")
            _state.value = _state.value.copy(isLocationPermissionGranted = true)
            startLocationUpdates()
        }

        /**
         * WebSocket 연결을 시작합니다.
         */
        private fun connectWebSocket() {
            Log.d(TAG, "WebSocket 연결 시작")
            connectWebSocketUseCase()
        }

        /**
         * WebSocket 연결 상태를 관찰하고 UI 상태를 업데이트합니다.
         */
        private fun observeWebSocketState() {
            observeWebSocketConnectionStateUseCase()
                .onEach { connectionState ->
                    Log.d(TAG, "WebSocket 연결 상태 변경: $connectionState")
                    _state.value = _state.value.copy(webSocketConnectionState = connectionState)
                }
                .launchIn(viewModelScope)
        }

        /**
         * 백그라운드에서 지속적으로 위치 업데이트를 받아옵니다.
         * 위치 권한이 허용된 경우에만 동작합니다.
         * 운행/대기 중일 때만 위치를 서버로 전송합니다.
         */
        private fun startLocationUpdates() {
            if (!getCurrentLocationUseCase.isLocationPermissionGranted()) {
                Log.d(TAG, "위치 권한이 없어 위치 업데이트를 시작할 수 없습니다.")
                return
            }

            Log.d(TAG, "백그라운드 위치 업데이트 시작")
            getLocationUpdatesUseCase()
                .combine(getDrivingStatusUseCase.drivingStatus) { location, drivingStatus ->
                    Pair(location, drivingStatus)
                }
                .onEach { (location, drivingStatus) ->
                    Log.d(
                        TAG,
                        "위치 업데이트 수신: 위도=${location.latitude}, 경도=${location.longitude}, 운행상태=$drivingStatus",
                    )
                    _state.value =
                        _state.value.copy(
                            currentLocation = location,
                            isLoading = false,
                            error = null,
                        )

                    // 운행/대기 중일 때만 위치를 서버로 전송
                    if (drivingStatus == DrivingStatus.Running || drivingStatus == DrivingStatus.Waiting) {
                        val busId = getBusIdUseCase()
                        val statusString = drivingStatus.toServerString()
                        Log.d(TAG, "운행 상태: $drivingStatus - 위치 전송")
                        sendLocationUseCase(busId, location, statusString)
                    } else {
                        Log.d(TAG, "운행 종료 상태 - 위치 전송 안 함")
                    }
                }
                .catch { exception ->
                    Log.e(TAG, "위치 업데이트 중 오류 발생", exception)
                    val message = errorMapper.run {
                        exception.toUserMessage()
                    }
                    _state.value = _state.value.copy(error = message)
                }
                .launchIn(viewModelScope)
        }

        /**
         * 운행 상태 변경을 관찰하여 즉시 상태를 서버로 전송합니다.
         * 운행 종료 시에도 상태를 DB에 저장하기 위함입니다.
         */
        /**
         * 운행 상태 변경을 관찰하여 즉시 상태를 서버로 전송합니다.
         * 운행 종료 시에도 상태를 DB에 저장하기 위함입니다.
         */
        private fun observeDrivingStatusChanges() {
            getDrivingStatusUseCase.drivingStatus
                .onEach { drivingStatus ->
                    // 현재 위치가 있으면 즉시 상태와 함께 전송
                    _state.value.currentLocation?.let { location ->
                        val busId = getBusIdUseCase()
                        val statusString = drivingStatus.toServerString()
                        Log.d(TAG, "운행 상태 변경: $drivingStatus - 즉시 전송")
                        sendLocationUseCase(busId, location, statusString)
                    }
                }
                .launchIn(viewModelScope)
        }
    }