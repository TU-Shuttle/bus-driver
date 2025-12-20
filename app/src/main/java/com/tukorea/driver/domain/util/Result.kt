package com.tukorea.driver.domain.util

import com.tukorea.driver.domain.error.AppError
import com.tukorea.driver.domain.error.MapError

/**
 * 비즈니스 로직의 실행 결과를 나타내는 sealed class
 * Domain 레이어에서 사용하여 성공/실패를 명시적으로 표현합니다.
 *
 * @param T 성공 시 반환되는 데이터 타입
 * @param E 에러 타입 (기본값: AppError)
 */
sealed class Result<out T, out E> {
    data class Success<T>(val data: T) : Result<T, Nothing>()

    data class Error<E>(val error: E) : Result<Nothing, E>()

    /**
     * 성공 여부를 확인합니다.
     * 간단한 boolean 체크가 필요할 때 사용합니다.
     */
    val isSuccess: Boolean
        get() = this is Success

    /**
     * 에러 여부를 확인합니다.
     * 간단한 boolean 체크가 필요할 때 사용합니다.
     */
    val isError: Boolean
        get() = this is Error

    /**
     * 성공 시 데이터를 반환하고, 실패 시 null을 반환합니다.
     * Optional 패턴처럼 사용할 때 유용합니다.
     * 예: result.getOrNull()?.let { ... }
     */
    fun getOrNull(): T? =
        when (this) {
            is Success -> data
            is Error -> null
        }
}

typealias AppResult<T> = Result<T, AppError>
typealias MapResult<T> = Result<T, MapError>
