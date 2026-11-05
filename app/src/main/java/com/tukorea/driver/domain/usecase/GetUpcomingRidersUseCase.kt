package com.tukorea.driver.domain.usecase

import com.tukorea.driver.domain.model.DepartureRiders
import com.tukorea.driver.domain.repository.RideSummaryRepository
import com.tukorea.driver.domain.util.AppResult
import com.tukorea.driver.domain.util.Result
import javax.inject.Inject

/** 지금 이후 출발편 중 탈 예정인 사람이 있는 것만, 가까운 순으로. */
class GetUpcomingRidersUseCase @Inject constructor(
    private val repository: RideSummaryRepository,
) {
    suspend operator fun invoke(nowMinute: Int, limit: Int = DEFAULT_LIMIT): AppResult<List<DepartureRiders>> =
        when (val result = repository.today()) {
            is Result.Error -> result
            is Result.Success -> Result.Success(select(result.data, nowMinute, limit))
        }

    fun select(all: List<DepartureRiders>, nowMinute: Int, limit: Int = DEFAULT_LIMIT): List<DepartureRiders> =
        all.filter { it.riders > 0 && it.departMinute >= nowMinute }
            .sortedBy { it.departMinute }
            .take(limit)

    private companion object {
        const val DEFAULT_LIMIT = 3
    }
}
