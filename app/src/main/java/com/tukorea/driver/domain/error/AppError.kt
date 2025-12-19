package com.tukorea.driver.domain.error

/**
 * 애플리케이션 전역 에러 타입
 * Domain 레이어에서 에러를 정의하여 UI 레이어에서 메시지로 변환합니다.
 */
sealed class AppError {
    object NetworkError : AppError()

    object UnknownError : AppError()

    data class CustomError(val errorMessage: String?) : AppError()
}
