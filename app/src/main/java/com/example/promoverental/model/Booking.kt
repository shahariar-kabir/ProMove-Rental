package com.example.promoverental.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Booking(
    val id: String? = null,
    @SerialName("user_id") val userId: String = "",
    @SerialName("house_id") val houseId: String = "",
    val status: String = "pending",
    @SerialName("created_at") val createdAt: String? = null,
    val house: House? = null // For joined data if we use postgrest join
)
