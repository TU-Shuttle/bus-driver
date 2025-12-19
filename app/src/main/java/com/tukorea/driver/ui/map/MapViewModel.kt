package com.tukorea.driver.ui.map

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tukorea.driver.domain.usecase.GetCurrentLocationUseCase
import com.tukorea.driver.domain.util.Result
import com.tukorea.driver.ui.common.ErrorMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
            loadCurrentLocation()
        }
    }
