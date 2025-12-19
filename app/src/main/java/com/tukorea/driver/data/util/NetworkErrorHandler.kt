package com.tukorea.driver.data.util

import com.tukorea.driver.domain.error.AppError
import com.tukorea.driver.domain.util.AppResult
import com.tukorea.driver.domain.util.Result
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object NetworkErrorHandler {
    /**
     * 네트워크 요청을 실행하고 에러를 Domain 에러로 변환합니다.
     *
     * @param block 실행할 네트워크 요청
     * @return 성공 시 데이터, 실패 시 에러를 포함한 Result
     */
    suspend fun <T> handleNetworkRequest(
        block: suspend () -> T
    ): AppResult<T> {
        return try {
            Result.Success(block())
        } catch (e: IOException) {
            Result.Error(AppError.NetworkError)

        } catch (e: SocketTimeoutException) {
            Result.Error(AppError.NetworkError)

        } catch (e: UnknownHostException) {
            Result.Error(AppError.NetworkError)

        } catch (e: Exception) {
            Result.Error(AppError.CustomError(e.message))
        }
    }
}

