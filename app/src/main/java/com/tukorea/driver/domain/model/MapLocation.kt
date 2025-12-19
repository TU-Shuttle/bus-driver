package com.tukorea.driver.domain.model

/**
 * 지도 위치 정보를 담는 데이터 모델
 * @param latitude 위도
 * @param longitude 경도
 * @param accuracy 위치 정확도 (미터 단위)
 */
data class MapLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float = 0f,
)
