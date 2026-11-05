package com.tukorea.driver.domain.repository

import com.tukorea.driver.domain.model.DepartureRiders
import com.tukorea.driver.domain.util.AppResult

interface RideSummaryRepository {
    /** 오늘(한국 시간) 출발편별 탑승 예정 인원. */
    suspend fun today(): AppResult<List<DepartureRiders>>
}
