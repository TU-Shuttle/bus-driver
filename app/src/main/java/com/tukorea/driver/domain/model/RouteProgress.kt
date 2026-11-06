package com.tukorea.driver.domain.model

/**
 * 운행 중인 노선에서 지금 어디쯤인지.
 * [nextStop]이 null이면 종점에 도착했다. [isOffRoute]면 고른 노선과 다른 길을 달리고 있다.
 */
data class RouteProgress(
    val nextStop: ShuttleStop?,
    val metersToNextStop: Double?,
    val isOffRoute: Boolean,
)
