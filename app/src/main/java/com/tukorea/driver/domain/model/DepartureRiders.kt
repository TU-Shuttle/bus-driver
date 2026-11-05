package com.tukorea.driver.domain.model

/** 출발 정류장·도착 정류장. 서버 방향 코드와 이름이 같다. */
enum class ShuttleStop { JEONGWANG_STATION, CAMPUS1, CAMPUS1_WEST_GATE, CAMPUS2 }

enum class ShuttleDirection(val origin: ShuttleStop, val destination: ShuttleStop) {
    STATION_TO_CAMPUS1(ShuttleStop.JEONGWANG_STATION, ShuttleStop.CAMPUS1),
    CAMPUS1_TO_STATION(ShuttleStop.CAMPUS1, ShuttleStop.JEONGWANG_STATION),
    CAMPUS1_TO_CAMPUS2(ShuttleStop.CAMPUS1, ShuttleStop.CAMPUS2),
    CAMPUS2_TO_CAMPUS1(ShuttleStop.CAMPUS2, ShuttleStop.CAMPUS1),
    STATION_TO_CAMPUS2_VIA_CAMPUS1(ShuttleStop.JEONGWANG_STATION, ShuttleStop.CAMPUS2),
    CAMPUS2_TO_STATION_VIA_CAMPUS1(ShuttleStop.CAMPUS2, ShuttleStop.JEONGWANG_STATION),
}

/**
 * 한 출발편에 탈 예정인 인원과 하차지별 인원.
 * @param departMinute 자정부터의 분
 */
data class DepartureRiders(
    val direction: ShuttleDirection,
    val departMinute: Int,
    val riders: Int,
    val dropOffs: Map<ShuttleStop, Int>,
)
