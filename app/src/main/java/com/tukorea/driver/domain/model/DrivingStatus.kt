package com.tukorea.driver.domain.model

/**
 * 운행 상태를 나타내는 enum
 * STOP: 운행 종료
 * RUNNING: 운행 중 (위치 전송)
 * WAITING: 대기 중 (위치 전송)
 */
enum class DrivingStatus {
    STOP, // 운행 종료
    RUNNING, // 운행 중
    WAITING, // 대기 중
    ;

    /**
     * 서버로 전송할 상태 문자열로 변환합니다.
     */
    fun toServerString(): String =
        when (this) {
            RUNNING -> "RUNNING"
            WAITING -> "WAITING"
            STOP -> "STOP"
        }
}
