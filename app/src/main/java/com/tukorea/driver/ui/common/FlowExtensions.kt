package com.tukorea.driver.ui.common

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch

/**
 * 공통적인 로딩/에러/성공 처리 패턴을 간소화하기 위한 Flow 확장 함수입니다.
 */
inline suspend fun <T, S> Flow<T>.collectWithUiState(
    state: MutableStateFlow<S>,
    crossinline onStart: (S) -> S,
    crossinline onError: (S, Throwable) -> S,
    crossinline onSuccess: (S, T) -> S
) {
    state.value = onStart(state.value)

    this
        .catch { throwable ->
            state.value = onError(state.value, throwable)
        }
        .collect { value ->
            state.value = onSuccess(state.value, value)
        }
}


