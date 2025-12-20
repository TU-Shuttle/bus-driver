package com.tukorea.driver.ui.auth

/**
 * 로그인 화면의 UI 상태
 */
data class LoginUiState(
    val vehicleNumber: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val vehicleNumberError: String? = null,
    val passwordError: String? = null,
)

