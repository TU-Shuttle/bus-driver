package com.tukorea.driver.data.api

import com.tukorea.driver.data.model.BusLocationResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {

    /**
     * 특정 버스의 최신 위치를 조회합니다.
     * @param id 버스/드라이버 ID
     * @return 버스 위치 정보 (없으면 null)
     */
    @GET("/api/bus/{id}/location")
    suspend fun getBusLocation(@Path("id") id: String): BusLocationResponse?

}
