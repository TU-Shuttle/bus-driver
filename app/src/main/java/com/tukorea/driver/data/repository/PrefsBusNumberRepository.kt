package com.tukorea.driver.data.repository

import android.content.Context
import com.tukorea.driver.domain.repository.BusNumberRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrefsBusNumberRepository @Inject constructor(
    @ApplicationContext context: Context,
) : BusNumberRepository {

    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private val _number = MutableStateFlow(
        prefs.getInt(KEY_NUMBER, 0).takeIf { it in BusNumberRepository.VALID },
    )
    override val number: StateFlow<Int?> = _number.asStateFlow()

    override fun set(number: Int) {
        require(number in BusNumberRepository.VALID) { "호차 번호는 1~99 입니다." }
        _number.value = number
        prefs.edit().putInt(KEY_NUMBER, number).apply()
    }

    private companion object {
        const val PREFS = "bus_number"
        const val KEY_NUMBER = "number"
    }
}
