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
import kotlinx.coroutines.launch
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

        // 운행 중 서버가 번호 중복으로 거절하면 위치 전송을 멈추고 기사에게 알린다.
        busNumberRepository.taken
            .onEach { number ->
                val state = _uiState.value
                if (number == state.busNumber && state.drivingStatus != DrivingStatus.Stop) {
                    getDrivingStatusUseCase.updateStatus(DrivingStatus.Stop)
                    _uiState.value = _uiState.value.copy(stoppedForDuplicate = number)
                }
            }
            .launchIn(viewModelScope)
    }

    /**
     * 번호를 서버에 확인하고 쓸 수 있으면 저장한다. 다른 기사가 운행 중이면 [HomeUiState.takenNumber]에 남긴다.
     * 서버에 닿지 않으면 일단 저장하고, 운행 중 서버 검사에 맡긴다.
     */
    fun claimBusNumber(number: Int, onClaimed: (Int) -> Unit) {
        if (_uiState.value.drivingStatus != DrivingStatus.Stop || number !in BusNumberRepository.VALID) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCheckingNumber = true, takenNumber = null)
            val available = busNumberRepository.isAvailable(number)
            if (available == false) {
                _uiState.value = _uiState.value.copy(isCheckingNumber = false, takenNumber = number)
                return@launch
            }
            busNumberRepository.set(number)
            _uiState.value = _uiState.value.copy(isCheckingNumber = false, stoppedForDuplicate = null)
            onClaimed(number)
        }
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
        _uiState.value = _uiState.value.copy(stoppedForDuplicate = null)
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
