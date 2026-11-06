package com.tukorea.driver.domain.usecase

import com.tukorea.driver.domain.model.GeoPoint
import com.tukorea.driver.domain.model.RouteProgress
import com.tukorea.driver.domain.model.ShuttleRoute
import javax.inject.Inject

/**
 * 현재 위치를 노선 위에 내려 다음 정류장과 남은 거리를 구한다.
 * 정류장을 [ARRIVED_METERS] 안쪽까지 지나면 그 다음 정류장으로 넘어간다.
 */
class TrackRouteProgressUseCase
    @Inject
    constructor() {
        operator fun invoke(
            route: ShuttleRoute,
            position: GeoPoint,
        ): RouteProgress? {
            val bus = route.project(position) ?: return null
            if (bus.offMeters > OFF_ROUTE_METERS) {
                return RouteProgress(nextStop = null, metersToNextStop = null, isOffRoute = true)
            }
            val next =
                route.direction.stops
                    .drop(1)
                    .mapNotNull { stop -> route.project(stop.point)?.let { stop to it.alongMeters } }
                    .firstOrNull { (_, along) -> along - bus.alongMeters > ARRIVED_METERS }
            return RouteProgress(
                nextStop = next?.first,
                metersToNextStop = next?.let { it.second - bus.alongMeters },
                isOffRoute = false,
            )
        }

        private companion object {
            const val OFF_ROUTE_METERS = 150.0
            const val ARRIVED_METERS = 40.0
        }
    }
