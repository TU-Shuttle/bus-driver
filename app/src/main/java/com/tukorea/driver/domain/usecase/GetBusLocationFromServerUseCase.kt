package com.tukorea.driver.domain.usecase

import com.tukorea.driver.domain.error.MapError
import com.tukorea.driver.domain.model.MapLocation
import com.tukorea.driver.domain.repository.MapRepository
import com.tukorea.driver.domain.util.MapResult
import com.tukorea.driver.domain.util.Result
import javax.inject.Inject

/**
 * 서버에서 특정 버스의 최신 위치를 조회하는 UseCase.
 * 비즈니스 로직(응답 null 체크 등)을 처리합니다.
 */
class GetBusLocationFromServerUseCase
    @Inject
    constructor(
        private val repository: MapRepository,
    ) {
        /**
         * 서버에서 버스 위치를 조회합니다.
         * @param busId 버스/드라이버 ID
         * @return 성공 시 위치 정보, 실패 시 에러를 포함한 Result
         */
        suspend operator fun invoke(busId: String): MapResult<MapLocation> {
            return when (val result = repository.getBusLocationFromServer(busId)) {
                is Result.Success -> {
                    val location = result.data
                    if (location == null) {
                        Result.Error(MapError.LocationNotFound)
                    } else {
                        Result.Success(location)
                    }
                }

                is Result.Error -> result
            }
        }
    }
