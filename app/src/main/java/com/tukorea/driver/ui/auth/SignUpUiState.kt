package com.tukorea.driver.ui.auth

/**
 * 회원가입 화면의 UI 상태
 */
data class SignUpUiState(
    val name: String = "",
    val vehicleNumber: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val nameError: String? = null,
    val vehicleNumberError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val passwordStrength: PasswordStrength = PasswordStrength.None,
)

/**
 * 비밀번호 강도
 */
enum class PasswordStrength {
    None,
    Weak,
    Medium,
    Strong,
}

