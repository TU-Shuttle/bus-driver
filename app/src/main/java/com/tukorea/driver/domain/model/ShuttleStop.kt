package com.tukorea.driver.domain.model

/** 셔틀 정류장. 좌표는 학생 앱과 같은 지도 표시용 근사값이다. */
enum class ShuttleStop(val point: GeoPoint) {
    JEONGWANG_STATION(GeoPoint(37.35187, 126.7415)),
    CAMPUS1(GeoPoint(37.33949, 126.7326)),
    CAMPUS1_WEST_GATE(GeoPoint(37.33862, 126.7340)),
    CAMPUS2(GeoPoint(37.32802, 126.6884)),
    OIDO_STATION(GeoPoint(37.36236, 126.73871)),
}
