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
         */
        operator fun invoke(busId: String, location: MapLocation, status: String) {
            repository.sendLocation(busId, location, status)
        }
    }
