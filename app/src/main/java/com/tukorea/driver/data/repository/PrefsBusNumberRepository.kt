package com.tukorea.driver.data.repository

import android.content.Context
import com.tukorea.driver.BuildConfig
import com.tukorea.driver.data.api.ApiService
import com.tukorea.driver.data.device.DeviceIdStore
import com.tukorea.driver.data.websocket.WebSocketDataSource
import com.tukorea.driver.domain.repository.BusNumberRepository
import com.tukorea.driver.domain.usecase.GetBusIdUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrefsBusNumberRepository
    @Inject
    constructor(
        @ApplicationContext context: Context,
        private val api: ApiService,
        private val deviceIdStore: DeviceIdStore,
        private val webSocket: WebSocketDataSource,
    ) : BusNumberRepository {
        private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

        private val _number =
            MutableStateFlow(
                prefs.getInt(KEY_NUMBER, 0).takeIf { it in BusNumberRepository.VALID },
            )
        override val number: StateFlow<Int?> = _number.asStateFlow()

        override fun set(number: Int) {
            require(number in BusNumberRepository.VALID) { "호차 번호는 1~99 입니다." }
            _number.value = number
            prefs.edit().putInt(KEY_NUMBER, number).apply()
        }

        override suspend fun isAvailable(number: Int): Boolean? =
            withContext(Dispatchers.IO) {
                runCatching {
                    api.checkAvailability(
                        GetBusIdUseCase.busIdOf(number),
                        deviceIdStore.id,
                        BuildConfig.DRIVER_TOKEN,
                    ).available
                }.getOrNull()
            }

        override val taken: Flow<Int> =
            webSocket.busNumberTaken.mapNotNull { id ->
                id.removePrefix("bus-").toIntOrNull()
            }

        private companion object {
            const val PREFS = "bus_number"
            const val KEY_NUMBER = "number"
        }
    }
