package com.tukorea.driver.domain.usecase

import com.tukorea.driver.domain.model.MapLocation
import com.tukorea.driver.domain.repository.MapRepository
import javax.inject.Inject

/**
 * 실시간 위치를 백엔드 서버로 전송하는 UseCase.
 */
class SendLocationUseCase
    @Inject
    constructor(
        private val repository: MapRepository,
    ) {
        /**
         * 위치 정보를 서버로 전송합니다.
         * @param busId 버스/드라이버 ID
         * @param location 위치 정보
         * @param status 운행 상태 ("RUNNING", "WAITING", "STOP")
         * @param direction 기사가 고른 노선 코드. 고르지 않았으면 null
         */
        operator fun invoke(
            busId: String,
            location: MapLocation,
            status: String,
            direction: String?,
        ) {
            repository.sendLocation(busId, location, status, direction)
        }
    }
