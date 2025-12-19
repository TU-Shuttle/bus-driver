package com.tukorea.driver.domain.repository

import com.tukorea.driver.domain.model.MapLocation
import com.tukorea.driver.domain.util.MapResult

interface MapRepository {
    /**
     * 현재 위치를 가져옵니다.
     * @return 성공 시 위치 정보, 실패 시 에러를 포함한 Result
     */
    suspend fun getCurrentLocation(): MapResult<MapLocation>

    /**
     * 위치 권한이 허용되었는지 확인합니다.
     * @return 위치 권한 허용 여부
     */
    fun isLocationPermissionGranted(): Boolean
}
