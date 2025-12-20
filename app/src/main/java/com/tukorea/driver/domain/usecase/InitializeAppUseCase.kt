package com.tukorea.driver.domain.usecase

import com.tukorea.driver.domain.error.AppError
import com.tukorea.driver.domain.repository.TempRepository
import com.tukorea.driver.domain.util.AppResult
import com.tukorea.driver.domain.util.Result
import javax.inject.Inject

class InitializeAppUseCase
    @Inject
    constructor() {
        /**
         * 앱 초기화를 수행합니다.
         *
         * @return 성공 시 Unit, 실패 시 에러를 포함한 Result
         */
        suspend operator fun invoke(): AppResult<Unit> {
            return try {
                // 실제 초기화 로직 추가 (예: 토큰 로드, 원격 설정, FCM 등록 등)
                Result.Success(Unit)
            } catch (e: IllegalStateException) {
                Result.Error(AppError.CustomError(e.message ?: "앱 초기화 상태 오류"))
            } catch (e: Exception) {
                // 예상치 못한 예외를 처리하기 위해 Exception을 사용
                Result.Error(AppError.CustomError(e.message ?: "앱 초기화 실패"))
            }
        }
    }
