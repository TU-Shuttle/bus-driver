package com.tukorea.driver.domain.usecase

import com.tukorea.driver.domain.repository.MapRepository
import javax.inject.Inject

/**
 * WebSocket 연결을 관리하는 UseCase.
 */
class ConnectWebSocketUseCase
    @Inject
    constructor(
        private val repository: MapRepository,
    ) {
        /**
         * WebSocket 연결을 시작합니다.
         */
        operator fun invoke() {
            repository.connectWebSocket()
        }
    }
