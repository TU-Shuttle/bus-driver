package com.tukorea.driver.domain.usecase

import javax.inject.Inject
import javax.inject.Singleton

/**
 * 현재 버스/드라이버 ID를 제공하는 UseCase.
 */
@Singleton
class GetBusIdUseCase
    @Inject
    constructor() {
        /**
         * 현재 버스/드라이버 ID를 반환합니다.
         * @return 버스/드라이버 ID
         */
        operator fun invoke(): String {
            // Note: 실제 구현 시 사용자 인증 정보나 설정에서 가져와야 합니다.
            return "bus-001"
        }
    }
