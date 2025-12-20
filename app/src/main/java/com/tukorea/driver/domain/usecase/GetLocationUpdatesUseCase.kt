package com.tukorea.driver.domain.usecase

import com.tukorea.driver.domain.model.MapLocation
import com.tukorea.driver.domain.repository.MapRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 백그라운드에서 지속적으로 위치 업데이트를 받아오는 UseCase.
 */
class GetLocationUpdatesUseCase
    @Inject
    constructor(
        private val repository: MapRepository,
    ) {
        /**
         * 위치 업데이트를 제공하는 Flow를 반환합니다.
         * @return 위치 업데이트를 제공하는 Flow
         */
        operator fun invoke(): Flow<MapLocation> = repository.getLocationUpdates()
    }
