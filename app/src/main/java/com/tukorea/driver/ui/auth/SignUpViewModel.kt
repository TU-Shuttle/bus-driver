package com.tukorea.driver.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * 회원가입 화면의 상태를 관리하는 ViewModel
 */
@HiltViewModel
class SignUpViewModel
    @Inject
    constructor() : ViewModel() {
        private val _uiState = MutableStateFlow(SignUpUiState())
        val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

        /**
         * 이름을 업데이트합니다.
         */
        fun updateName(name: String) {
            val nameError = validateName(name)
            _uiState.value = _uiState.value.copy(
                name = name,
                nameError = nameError,
                error = null,
            )
        }

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
            val passwordStrength = calculatePasswordStrength(password)
            _uiState.value = _uiState.value.copy(
                password = password,
                passwordError = passwordError,
                passwordStrength = passwordStrength,
                error = null,
            )
            // 비밀번호가 변경되면 확인 비밀번호도 다시 검증
            if (_uiState.value.confirmPassword.isNotBlank()) {
                updateConfirmPassword(_uiState.value.confirmPassword)
            }
        }

        /**
         * 비밀번호 확인을 업데이트합니다.
         */
        fun updateConfirmPassword(confirmPassword: String) {
            val confirmPasswordError = validateConfirmPassword(
                password = _uiState.value.password,
                confirmPassword = confirmPassword,
            )
            _uiState.value = _uiState.value.copy(
                confirmPassword = confirmPassword,
                confirmPasswordError = confirmPasswordError,
                error = null,
            )
        }

        /**
         * 이름 검증
         */
        private fun validateName(name: String): String? {
            return when {
                name.isBlank() -> null
                name.length < 2 -> "이름은 2자 이상이어야 합니다"
                else -> null
            }
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
                !password.any { it.isDigit() } -> "비밀번호에 숫자가 포함되어야 합니다"
                !password.any { it.isLetter() } -> "비밀번호에 영문자가 포함되어야 합니다"
                else -> null
            }
        }

        /**
         * 비밀번호 확인 검증
         */
        private fun validateConfirmPassword(password: String, confirmPassword: String): String? {
            return when {
                confirmPassword.isBlank() -> null
                password != confirmPassword -> "비밀번호가 일치하지 않습니다"
                else -> null
            }
        }

        /**
         * 비밀번호 강도 계산
         */
        private fun calculatePasswordStrength(password: String): PasswordStrength {
            if (password.isBlank()) return PasswordStrength.None

            var strength = 0
            if (password.length >= 8) strength++
            if (password.any { it.isDigit() }) strength++
            if (password.any { it.isLowerCase() }) strength++
            if (password.any { it.isUpperCase() }) strength++
            if (password.any { !it.isLetterOrDigit() }) strength++

            return when {
                strength <= 2 -> PasswordStrength.Weak
                strength <= 3 -> PasswordStrength.Medium
                else -> PasswordStrength.Strong
            }
        }

        /**
         * 비밀번호 표시/숨기기를 토글합니다.
         */
        fun togglePasswordVisibility() {
            _uiState.value = _uiState.value.copy(isPasswordVisible = !_uiState.value.isPasswordVisible)
        }

        /**
         * 비밀번호 확인 표시/숨기기를 토글합니다.
         */
        fun toggleConfirmPasswordVisibility() {
            _uiState.value = _uiState.value.copy(isConfirmPasswordVisible = !_uiState.value.isConfirmPasswordVisible)
        }

        /**
         * 회원가입을 시도합니다.
         */
        fun signUp() {
            // TODO: 실제 회원가입 로직 구현
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        }
    }
