package com.tukorea.driver.ui.home

import com.tukorea.driver.domain.usecase.GetUpcomingRidersUseCase
import com.tukorea.driver.domain.util.Result
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone
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
    private val getUpcomingRiders: GetUpcomingRidersUseCase,
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

        // 학생들이 남긴 탑승 예정 인원을 주기적으로 갱신한다.
        viewModelScope.launch {
            while (true) {
                val result = getUpcomingRiders(nowMinuteInSeoul())
                _uiState.update {
                    it.copy(upcomingRiders = (result as? Result.Success)?.data ?: it.upcomingRiders)
                }
                delay(RIDERS_REFRESH_MS)
            }
        }
    }

    private fun nowMinuteInSeoul(): Int {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"))
        return calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
    }

    private companion object {
        const val RIDERS_REFRESH_MS = 30_000L
    }

    /**
     * 홈 화면에서 사용하는 모달(bottom sheet)의 현재 높이 상태를 갱신합니다.
     *
     * @param height 현재 모달의 높이(열림/반쯤/닫힘 등)를 나타내는 값
     */
    fun updateModalHeight(height: ModalHeight) {
        _uiState.value = _uiState.value.copy(modalHeight = height)
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
