package com.tukorea.driver.ui.temp

import com.tukorea.driver.domain.model.Temp

data class TempUiState(
    val isLoading: Boolean = false,
    val temps: List<Temp> = emptyList(),
    val error: String? = null,
)
