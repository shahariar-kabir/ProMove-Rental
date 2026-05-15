package com.example.promoverental.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: String? = null,
    @SerialName("sender_id") val senderId: String = "",
    @SerialName("receiver_id") val receiverId: String = "",
    val message: String = "",
    @SerialName("created_at") val createdAt: String? = null
)
