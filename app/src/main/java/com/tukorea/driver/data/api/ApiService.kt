package com.tukorea.driver.data.api

import com.tukorea.driver.data.model.BusLocationResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

data class AvailabilityResponse(val available: Boolean?)

interface ApiService {

    /**
     * 특정 버스의 최신 위치를 조회합니다.
     * @param id 버스/드라이버 ID
     * @return 버스 위치 정보 (없으면 null)
     */
    @GET("/api/bus/{id}/location")
    suspend fun getBusLocation(@Path("id") id: String): BusLocationResponse?

    /** 이 기기가 이 버스 id(호차 번호)를 쓸 수 있는지. 다른 기사가 운행 중이면 false. */
    @GET("/api/bus/{id}/availability")
    suspend fun checkAvailability(
        @Path("id") id: String,
        @Query("deviceId") deviceId: String,
        @Header("X-Driver-Token") token: String,
    ): AvailabilityResponse

}
