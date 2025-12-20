package com.tukorea.driver.ui.home

import com.tukorea.driver.domain.model.DrivingStatus

/**
 * 홈 화면의 UI 상태를 나타내는 데이터 클래스
 */
data class HomeUiState(
    val modalHeight: ModalHeight = ModalHeight.MID,
    val isModalVisible: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null,
    val drivingStatus: DrivingStatus = DrivingStatus.Stop,
)

/**
 * 모달 높이를 나타내는 enum
 * LOW: 80dp (드래그 바만 보임)
 * MID: 화면의 40% (중간 높이, 시작 상태)
 * HIGH: 화면의 75% (최대 높이, 상단 바에 안 가림)
 */
enum class ModalHeight {
    LOW,
    MID,
    HIGH,
}
