package com.tukorea.driver.domain.usecase

import com.tukorea.driver.domain.repository.MapRepository
import javax.inject.Inject

/**
 * WebSocket 연결을 종료하는 UseCase.
 */
class DisconnectWebSocketUseCase
    @Inject
    constructor(
        private val repository: MapRepository,
    ) {
        /**
         * WebSocket 연결을 종료합니다.
         */
        operator fun invoke() {
            repository.disconnectWebSocket()
        }
    }
