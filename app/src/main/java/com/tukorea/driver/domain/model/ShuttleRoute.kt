package com.tukorea.driver.domain.model

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot

data class GeoPoint(val latitude: Double, val longitude: Double)

/**
 * 한 방향의 실제 도로 경로(근사). 지도에 선으로 그리고, 버스가 경로 위 어디쯤인지 계산하는 데 쓴다.
 * 캠퍼스 주변 몇 km 범위라 평면 근사(등장방형)로 충분하다.
 */
data class ShuttleRoute(val direction: ShuttleDirection, val points: List<GeoPoint>) {
    /** 각 점까지의 누적 거리(m). */
    private val cumulative: DoubleArray =
        DoubleArray(points.size).also { acc ->
            for (i in 1 until points.size) acc[i] = acc[i - 1] + distanceMeters(points[i - 1], points[i])
        }

    val lengthMeters: Double get() = cumulative.lastOrNull() ?: 0.0

    /** [point]를 경로에 수직으로 내린 위치. [alongMeters]는 출발점부터의 거리, [offMeters]는 경로에서 떨어진 거리. */
    fun project(point: GeoPoint): Projection? {
        if (points.size < 2) return null
        var best: Projection? = null
        for (i in 0 until points.size - 1) {
            val a = points[i]
            val b = points[i + 1]
            val (ax, ay) = toLocal(a, a)
            val (bx, by) = toLocal(b, a)
            val (px, py) = toLocal(point, a)
            val len2 = (bx - ax) * (bx - ax) + (by - ay) * (by - ay)
            val t =
                if (len2 == 0.0) {
                    0.0
                } else {
                    (((px - ax) * (bx - ax) + (py - ay) * (by - ay)) / len2).coerceIn(
                        0.0,
                        1.0,
                    )
                }
            val off = hypot(px - (ax + (bx - ax) * t), py - (ay + (by - ay) * t))
            if (best == null || off < best.offMeters) {
                best =
                    Projection(
                        alongMeters = cumulative[i] + (cumulative[i + 1] - cumulative[i]) * t,
                        offMeters = off,
                    )
            }
        }
        return best
    }

    data class Projection(val alongMeters: Double, val offMeters: Double)
}

private const val METERS_PER_DEGREE = 111_320.0
private const val FULL_TURN = 360.0

private fun toLocal(
    p: GeoPoint,
    origin: GeoPoint,
): Pair<Double, Double> {
    val x = (p.longitude - origin.longitude) * METERS_PER_DEGREE * cos(Math.toRadians(origin.latitude))
    val y = (p.latitude - origin.latitude) * METERS_PER_DEGREE
    return x to y
}

fun distanceMeters(
    a: GeoPoint,
    b: GeoPoint,
): Double {
    val (x, y) = toLocal(b, a)
    return hypot(x, y)
}

/** a→b 진행 방향. 북쪽 0°, 시계 방향(지도 마커 회전값과 같은 기준). */
fun bearingDegrees(
    a: GeoPoint,
    b: GeoPoint,
): Double {
    val (x, y) = toLocal(b, a)
    return (Math.toDegrees(atan2(x, y)) + FULL_TURN) % FULL_TURN
}
