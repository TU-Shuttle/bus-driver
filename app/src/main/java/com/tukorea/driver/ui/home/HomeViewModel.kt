package com.tukorea.driver.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tukorea.driver.data.location.LocationServiceManager
import com.tukorea.driver.domain.model.DrivingStatus
import com.tukorea.driver.domain.model.ShuttleDirection
import com.tukorea.driver.domain.repository.BusNumberRepository
import com.tukorea.driver.domain.repository.RouteRepository
import com.tukorea.driver.domain.usecase.GetDrivingStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

/**
 * 홈 화면의 상태를 관리하는 ViewModel
 */
@HiltViewModel
class HomeViewModel
@Inject
constructor(
    private val getDrivingStatusUseCase: GetDrivingStatusUseCase,
    private val locationServiceManager: LocationServiceManager,
    private val routeRepository: RouteRepository,
    private val busNumberRepository: BusNumberRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        // 운행 상태를 관찰하여 UI 상태 업데이트 및 Foreground Service 제어
        getDrivingStatusUseCase.drivingStatus
            .onEach { status ->
                _uiState.value = _uiState.value.copy(drivingStatus = status)
                
                // 운행/대기 중일 때만 Foreground Service 시작
                when (status) {
                    DrivingStatus.Running, DrivingStatus.Waiting -> {
                        locationServiceManager.startService()
                    }
                    DrivingStatus.Stop -> {
                        locationServiceManager.stopService()
                    }
                }
            }
            .launchIn(viewModelScope)

        routeRepository.selectedDirection
            .onEach { direction -> _uiState.value = _uiState.value.copy(direction = direction) }
            .launchIn(viewModelScope)
    }

    init {
        busNumberRepository.number
            .onEach { number -> _uiState.value = _uiState.value.copy(busNumber = number) }
            .launchIn(viewModelScope)
    }

    /**
     * 호차 번호를 정한다. 운행 중에 바꾸면 학생 앱에 이전 번호 버스가 잠시 남으므로 운행 전에만 바꿀 수 있다.
     */
    fun setBusNumber(number: Int) {
        if (_uiState.value.drivingStatus != DrivingStatus.Stop) return
        if (number !in BusNumberRepository.VALID) return
        busNumberRepository.set(number)
    }

    /** 운행 노선을 고른다. 운행 중에 바꾸면(예: 종점에서 반대 방향으로) 바로 학생 앱에 반영된다. */
    fun selectDirection(direction: ShuttleDirection) {
        routeRepository.select(direction)
    }




    /**
     * 운행을 시작합니다.
     */
    fun startDriving() {
        // 화면 상태가 아니라 저장소를 직접 본다(번호·노선을 고른 직후 바로 시작하는 경우).
        if (routeRepository.selectedDirection.value == null || busNumberRepository.number.value == null) return
        getDrivingStatusUseCase.updateStatus(DrivingStatus.Running)
    }

    /**
     * 대기 상태로 전환합니다.
     */
    fun startWaiting() {
        getDrivingStatusUseCase.updateStatus(DrivingStatus.Waiting)
    }

    /**
     * 운행을 종료합니다.
     */
    fun stopDriving() {
        getDrivingStatusUseCase.updateStatus(DrivingStatus.Stop)
    }
}
