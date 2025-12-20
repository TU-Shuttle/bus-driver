package com.tukorea.driver.domain.usecase

import com.tukorea.driver.domain.model.DrivingStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 운행 상태를 관찰하는 UseCase
 */
@Singleton
class GetDrivingStatusUseCase
@Inject
constructor() {
    private val _drivingStatus = kotlinx.coroutines.flow.MutableStateFlow(DrivingStatus.Stop)
    val drivingStatus: Flow<DrivingStatus> = _drivingStatus

    /**
     * 운행 상태를 업데이트합니다.
     */
    fun updateStatus(status: DrivingStatus) {
        _drivingStatus.value = status
    }
}
