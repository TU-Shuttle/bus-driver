package com.tukorea.driver.domain.model

/**
 * 기사가 운행 시작 전에 고르는 노선. 이름은 서버 시간표의 방향 코드와 같다.
 * [stops]는 지나는 순서대로의 정류장이다.
 */
enum class ShuttleDirection(val stops: List<ShuttleStop>) {
    STATION_TO_CAMPUS1(listOf(ShuttleStop.JEONGWANG_STATION, ShuttleStop.CAMPUS1)),
    CAMPUS1_TO_STATION(listOf(ShuttleStop.CAMPUS1, ShuttleStop.JEONGWANG_STATION)),
    CAMPUS1_TO_CAMPUS2(listOf(ShuttleStop.CAMPUS1, ShuttleStop.CAMPUS2)),
    CAMPUS2_TO_CAMPUS1(listOf(ShuttleStop.CAMPUS2, ShuttleStop.CAMPUS1)),
    STATION_TO_CAMPUS2_VIA_CAMPUS1(
        listOf(ShuttleStop.JEONGWANG_STATION, ShuttleStop.CAMPUS1_WEST_GATE, ShuttleStop.CAMPUS2),
    ),
    CAMPUS2_TO_STATION_VIA_CAMPUS1(
        listOf(ShuttleStop.CAMPUS2, ShuttleStop.CAMPUS1_WEST_GATE, ShuttleStop.JEONGWANG_STATION),
    ),
    CAMPUS2_TO_OIDO_STATION(listOf(ShuttleStop.CAMPUS2, ShuttleStop.OIDO_STATION)),
    ;

    val origin: ShuttleStop get() = stops.first()
    val destination: ShuttleStop get() = stops.last()
}
