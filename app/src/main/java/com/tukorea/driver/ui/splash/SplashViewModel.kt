package com.tukorea.driver.ui.splash

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tukorea.driver.domain.usecase.InitializeAppUseCase
import com.tukorea.driver.domain.util.Result
import com.tukorea.driver.ui.common.ErrorMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 앱 초기화(토큰 확인, 기본 데이터 로드 등)를 담당하는 Splash ViewModel.
 */
@HiltViewModel
class SplashViewModel
    @Inject
    constructor(
        private val initializeAppUseCase: InitializeAppUseCase,
        private val errorMapper: ErrorMapper,
    ) : ViewModel() {
        private val _state = MutableStateFlow(SplashUiState())
        val state: StateFlow<SplashUiState> = _state

        private companion object {
            private const val TAG = "SplashViewModel"
        }

        fun initialize() {
            Log.d(TAG, "앱 초기화 시작")
            viewModelScope.launch {
                when (val result = initializeAppUseCase()) {
                    is Result.Success -> {
                        Log.d(TAG, "앱 초기화 성공")
                        _state.value =
                            _state.value.copy(
                                isInitialized = true,
                                error = null,
                            )
                    }

                    is Result.Error -> {
                        val message = errorMapper.run { result.error.toUserMessage() }
                        Log.w(TAG, "앱 초기화 실패: 메시지=$message, 원본오류=${result.error}")
                        _state.value =
                            _state.value.copy(
                                isInitialized = true,
                                error = message,
                            )
                    }
                }
            }
        }
    }
