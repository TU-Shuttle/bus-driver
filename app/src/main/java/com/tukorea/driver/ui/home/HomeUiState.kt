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
    /** 기사가 정한 호차 번호. 정하기 전엔 운행을 시작할 수 없다. */
    val busNumber: Int? = null,
    /** 번호 입력 창에서 서버에 확인하는 중. */
    val isCheckingNumber: Boolean = false,
    /** 다른 기사가 운행 중이라 쓸 수 없는 번호(입력 창에 알림). */
    val takenNumber: Int? = null,
    /** 운행 중 다른 기사와 번호가 겹쳐 운행을 멈춘 경우 그 번호(시트에 알림). */
    val stoppedForDuplicate: Int? = null,
)
