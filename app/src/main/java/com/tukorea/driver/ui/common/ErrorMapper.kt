package com.tukorea.driver.ui.common

import android.content.Context
import com.tukorea.driver.R
import com.tukorea.driver.domain.error.AppError
import com.tukorea.driver.domain.error.MapError
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 도메인 에러(AppError, MapError, Throwable)를
 * UI에서 사용할 사용자용 문자열(String)로 변환하는 매퍼 클래스입니다.
 *
 * - 도메인 레이어는 문자열을 모르고, 타입(AppError/MapError)만 정의합니다.
 * - UI 레이어(ViewModel, Screen)는 이 매퍼를 통해 string.xml 기반 메시지를 사용합니다.
 * - Context는 DI를 통해 주입받아 테스트 가능성을 높입니다.
 */
@Singleton
class ErrorMapper
    @Inject
    constructor(
        private val context: Context,
    ) {
        /**
         * AppError를 사용자용 메시지로 변환합니다.
         */
        fun AppError.toUserMessage(): String =
            when (this) {
                AppError.NetworkError ->
                    context.getString(R.string.error_network)

                AppError.UnknownError ->
                    context.getString(R.string.error_unknown)

                is AppError.CustomError ->
                    this.errorMessage ?: context.getString(R.string.error_generic)
            }

        /**
         * MapError를 사용자용 메시지로 변환합니다.
         */
        fun MapError.toUserMessage(): String =
            when (this) {
                MapError.LocationNotFound ->
                    context.getString(R.string.error_location_not_found)

                MapError.PermissionDenied ->
                    context.getString(R.string.error_location_permission)

                MapError.NetworkError ->
                    context.getString(R.string.error_network)

                MapError.Timeout ->
                    context.getString(R.string.error_location_timeout)

                is MapError.Unknown ->
                    this.errorMessage ?: context.getString(R.string.error_unknown)
            }

        /**
         * 아직 도메인 에러로 매핑되지 않은 예외를 위한 기본 매퍼입니다.
         * (주로 Flow.catch { } 나 try-catch 에서 사용)
         */
        fun Throwable.toUserMessage(): String =
            when (this) {
                is SocketTimeoutException ->
                    context.getString(R.string.error_timeout)

                is UnknownHostException, is IOException ->
                    context.getString(R.string.error_network)

                else ->
                    this.message ?: context.getString(R.string.error_unknown)
            }
    }
