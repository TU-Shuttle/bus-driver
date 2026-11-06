package com.tukorea.driver.ui.home

import com.tukorea.driver.domain.model.DrivingStatus
import com.tukorea.driver.domain.model.ShuttleDirection

/**
 * 홈 화면의 UI 상태를 나타내는 데이터 클래스
 */
data class HomeUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val drivingStatus: DrivingStatus = DrivingStatus.Stop,
    /** 기사가 고른 노선. 고르기 전엔 운행을 시작할 수 없다. */
    val direction: ShuttleDirection? = null,
)
