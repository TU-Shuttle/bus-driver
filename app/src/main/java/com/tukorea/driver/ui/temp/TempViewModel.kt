package com.tukorea.driver.ui.temp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tukorea.driver.domain.usecase.GetTempListUseCase
import com.tukorea.driver.domain.util.Result
import com.tukorea.driver.ui.common.ErrorMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TempViewModel @Inject constructor(
    private val getTempListUseCase: GetTempListUseCase,
    private val errorMapper: ErrorMapper
) : ViewModel() {

    private val _state = MutableStateFlow(TempUiState())
    val state: StateFlow<TempUiState> = _state

    private companion object {
        private const val TAG = "TempViewModel"
    }

    fun loadTemps() {
        Log.d(TAG, "임시 리스트 불러오기 시작")
        viewModelScope.launch {
            _state.value = TempUiState(isLoading = true, error = null)

            when (val result = getTempListUseCase()) {
                is Result.Success -> {
                    Log.d(TAG, "임시 리스트 불러오기 성공: 개수=${result.data.size}")
                    _state.value = TempUiState(
                        isLoading = false,
                        temps = result.data
                    )
                }

                is Result.Error -> {
                    val message = errorMapper.run { result.error.toUserMessage() }
                    Log.w(TAG, "임시 리스트 불러오기 실패: 메시지=$message, 원본오류=${result.error}")
                    _state.value = TempUiState(
                        isLoading = false,
                        error = message
                    )
                }
            }
        }
    }
}