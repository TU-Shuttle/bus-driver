package com.tukorea.driver.data.util

import android.util.Log
import com.tukorea.driver.domain.error.AppError
import com.tukorea.driver.domain.error.MapError
import com.tukorea.driver.domain.util.AppResult
import com.tukorea.driver.domain.util.MapResult
import com.tukorea.driver.domain.util.Result
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object NetworkErrorHandler {
    private const val TAG = "NetworkErrorHandler"

    /**
     * 네트워크 요청을 실행하고 에러를 AppError로 변환합니다.
     * AppResult를 반환하는 Repository에서 사용합니다.
     *
     * @param block 실행할 네트워크 요청
     * @return 성공 시 데이터, 실패 시 에러를 포함한 Result
     */
    suspend fun <T> handleNetworkRequest(block: suspend () -> T): AppResult<T> {
        return try {
            Result.Success(block())
        } catch (_: IOException) {
            Result.Error(AppError.NetworkError)
        } catch (_: SocketTimeoutException) {
            Result.Error(AppError.NetworkError)
        } catch (_: UnknownHostException) {
            Result.Error(AppError.NetworkError)
        } catch (e: Exception) {
            Result.Error(AppError.CustomError(e.message ?: "알 수 없는 오류"))
        }
    }

    /**
     * 네트워크 요청을 실행하고 에러를 MapError로 변환합니다.
     * MapResult를 반환하는 Repository에서 사용합니다.
     *
     * @param block 실행할 네트워크 요청 (MapResult를 반환)
     * @param tag 로깅용 태그 (선택사항)
     * @return 성공 시 데이터, 실패 시 에러를 포함한 Result
     */
    suspend fun <T> handleMapNetworkRequest(
        block: suspend () -> MapResult<T>,
        tag: String? = null,
    ): MapResult<T> {
        return try {
            block()
        } catch (e: IOException) {
            if (tag != null) {
                Log.e(tag, "네트워크 오류 발생", e)
            }
            Result.Error(MapError.NetworkError)
        } catch (e: SocketTimeoutException) {
            if (tag != null) {
                Log.e(tag, "네트워크 타임아웃", e)
            }
            Result.Error(MapError.Timeout)
        } catch (e: UnknownHostException) {
            if (tag != null) {
                Log.e(tag, "호스트를 찾을 수 없음", e)
            }
            Result.Error(MapError.NetworkError)
        } catch (e: Exception) {
            if (tag != null) {
                Log.e(tag, "네트워크 요청 실패", e)
            }
            Result.Error(MapError.Unknown(e.message))
        }
    }
}
