package com.tukorea.driver.domain.repository

import kotlinx.coroutines.flow.StateFlow

/** 기사가 직접 정한 호차 번호(1~99). 앱을 다시 켜도 유지된다. */
interface BusNumberRepository {
    val number: StateFlow<Int?>

    fun set(number: Int)

    companion object {
        val VALID = 1..99
    }
}
