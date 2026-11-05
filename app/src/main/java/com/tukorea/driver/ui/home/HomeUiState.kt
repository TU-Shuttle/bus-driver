package com.tukorea.driver.ui.home

import com.tukorea.driver.domain.model.DrivingStatus

/**
 * 홈 화면의 UI 상태를 나타내는 데이터 클래스
 */
data class HomeUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val drivingStatus: DrivingStatus = DrivingStatus.Stop,
)
