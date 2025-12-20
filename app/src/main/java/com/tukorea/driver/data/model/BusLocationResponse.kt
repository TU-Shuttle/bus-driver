package com.tukorea.driver.data.model

import com.google.gson.annotations.SerializedName

/**
 * 서버로부터 받은 버스 위치 정보 응답 DTO
 */
data class BusLocationResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName("lat")
    val lat: Double,
    @SerializedName("lng")
    val lng: Double,
    @SerializedName("accuracy")
    val accuracy: Float,
    @SerializedName("updatedAt")
    val updatedAt: Long,
)
