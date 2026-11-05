package com.tukorea.driver.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tukorea.driver.data.location.LocationServiceManager
import com.tukorea.driver.domain.model.DrivingStatus
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

    }




    /**
     * 운행을 시작합니다.
     */
    fun startDriving() {
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
