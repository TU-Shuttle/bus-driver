package com.tukorea.driver.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/** 기사가 직접 정한 호차 번호(1~99). 앱을 다시 켜도 유지된다. */
interface BusNumberRepository {
    val number: StateFlow<Int?>

    fun set(number: Int)

    /** 서버에 물어 이 번호를 쓸 수 있는지. 서버에 닿지 않으면 null(막지 않고 운행 중 서버 검사에 맡긴다). */
    suspend fun isAvailable(number: Int): Boolean?

    /** 운행 중 서버가 "다른 기사가 이 번호로 운행 중"이라며 거절한 호차 번호. */
    val taken: Flow<Int>

    companion object {
        val VALID = 1..99
    }
}
