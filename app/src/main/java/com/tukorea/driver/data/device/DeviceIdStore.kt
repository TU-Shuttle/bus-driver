package com.tukorea.driver.data.device

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 이 기사 앱 설치를 가리키는 무작위 id. 서버가 같은 호차 번호를 두 기기가 쓰지 못하게 할 때만 쓴다.
 * 기기 정보(IMEI 등)를 쓰지 않고, 앱을 지우면 새로 만들어진다.
 */
@Singleton
class DeviceIdStore
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) {
        private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

        val id: String by lazy {
            prefs.getString(
                KEY_ID,
                null,
            ) ?: UUID.randomUUID().toString().also { prefs.edit().putString(KEY_ID, it).apply() }
        }

        private companion object {
            const val PREFS = "device"
            const val KEY_ID = "id"
        }
    }
