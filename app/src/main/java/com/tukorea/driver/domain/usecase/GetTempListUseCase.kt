package com.tukorea.driver.domain.usecase

import com.tukorea.driver.domain.model.Temp
import com.tukorea.driver.domain.repository.TempRepository
import com.tukorea.driver.domain.util.AppResult
import javax.inject.Inject

/**
 * 임시 데이터 목록을 가져오는 UseCase.
 */
class GetTempListUseCase
    @Inject
    constructor(
        private val repository: TempRepository,
    ) {
        /**
         * 임시 데이터 목록을 반환합니다.
         *
         * @return 임시 데이터 목록을 포함한 Result
         */
        suspend operator fun invoke(): AppResult<List<Temp>> = repository.getTemps()
    }
