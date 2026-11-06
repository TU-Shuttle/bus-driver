package com.tukorea.driver.domain.repository

import com.tukorea.driver.domain.model.ShuttleDirection
import com.tukorea.driver.domain.model.ShuttleRoute
import kotlinx.coroutines.flow.StateFlow

/** 노선 경로와, 기사가 고른 노선(앱을 다시 켜도 유지). */
interface RouteRepository {
    val selectedDirection: StateFlow<ShuttleDirection?>

    fun select(direction: ShuttleDirection)

    fun routeOf(direction: ShuttleDirection): ShuttleRoute?
}
