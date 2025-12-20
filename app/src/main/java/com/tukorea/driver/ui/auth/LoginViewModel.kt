package com.tukorea.driver.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * 로그인 화면의 상태를 관리하는 ViewModel
 */
@HiltViewModel
class LoginViewModel
    @Inject
    constructor() : ViewModel() {
        private val _uiState = MutableStateFlow(LoginUiState())
        val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

        /**
         * 차량 번호를 업데이트합니다.
         */
        fun updateVehicleNumber(vehicleNumber: String) {
            val vehicleNumberError = validateVehicleNumber(vehicleNumber)
            _uiState.value = _uiState.value.copy(
                vehicleNumber = vehicleNumber,
                vehicleNumberError = vehicleNumberError,
                error = null,
            )
        }

        /**
         * 비밀번호를 업데이트합니다.
         */
        fun updatePassword(password: String) {
            val passwordError = validatePassword(password)
            _uiState.value = _uiState.value.copy(
                password = password,
                passwordError = passwordError,
                error = null,
            )
        }

        /**
         * 차량 번호 형식 검증
         */
        private fun validateVehicleNumber(vehicleNumber: String): String? {
            return when {
                vehicleNumber.isBlank() -> null
                vehicleNumber.length < 4 -> "차량 번호는 4자 이상이어야 합니다"
                !vehicleNumber.matches(Regex("^[0-9가-힣]+$")) -> "차량 번호는 숫자와 한글만 입력 가능합니다"
                else -> null
            }
        }

        /**
         * 비밀번호 검증
         */
        private fun validatePassword(password: String): String? {
            return when {
                password.isBlank() -> null
                password.length < 8 -> "비밀번호는 8자 이상이어야 합니다"
                else -> null
            }
        }

        /**
         * 비밀번호 표시/숨기기를 토글합니다.
         */
        fun togglePasswordVisibility() {
            _uiState.value = _uiState.value.copy(isPasswordVisible = !_uiState.value.isPasswordVisible)
        }

        /**
         * 로그인을 시도합니다.
         */
        fun login() {
            // TODO: 실제 로그인 로직 구현
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        }
    }
