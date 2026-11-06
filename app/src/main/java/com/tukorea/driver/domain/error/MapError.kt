package com.tukorea.driver.domain.error

/**
 * 맵 관련 에러 타입
 * Domain 레이어에서 에러를 정의하여 UI 레이어에서 메시지로 변환합니다.
 */
sealed class MapError {
    object LocationNotFound : MapError()

    object PermissionDenied : MapError()

    object NetworkError : MapError()

    object Timeout : MapError()

    data class Unknown(val errorMessage: String?) : MapError()
}
