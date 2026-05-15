package com.example.promoverental.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Favorite(
    val id: String? = null,
    @SerialName("user_id") val userId: String = "",
    @SerialName("house_id") val houseId: String = "",
    val house: House? = null
)
