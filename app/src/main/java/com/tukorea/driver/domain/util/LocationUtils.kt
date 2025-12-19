package com.tukorea.driver.domain.util

import com.tukorea.driver.domain.model.MapLocation
import kotlin.math.*


object LocationUtils {
    private const val EARTH_RADIUS_METERS = 6371000.0
    
    /**
     * 두 위치 간의 거리를 계산합니다 (미터 단위).
     * Haversine 공식
     * 
     * @param from 시작 위치
     * @param to 끝 위치
     * @return 거리 (미터)
     */
    fun calculateDistance(from: MapLocation, to: MapLocation): Double {
        val lat1Rad = Math.toRadians(from.latitude)
        val lat2Rad = Math.toRadians(to.latitude)
        val deltaLatRad = Math.toRadians(to.latitude - from.latitude)
        val deltaLngRad = Math.toRadians(to.longitude - from.longitude)
        
        val a = sin(deltaLatRad / 2).pow(2) +
                cos(lat1Rad) * cos(lat2Rad) *
                sin(deltaLngRad / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        
        return EARTH_RADIUS_METERS * c
    }
    
    /**
     * 두 위치가 지정된 거리(미터) 이상 떨어져 있는지 확인합니다.
     * @param from 시작 위치
     * @param to 끝 위치
     * @param thresholdMeters 임계 거리 (미터)
     * @return 임계 거리 이상 떨어져 있으면 true
     */
    fun isDistanceGreaterThan(
        from: MapLocation,
        to: MapLocation,
        thresholdMeters: Double
    ): Boolean {
        return calculateDistance(from, to) > thresholdMeters
    }
}

