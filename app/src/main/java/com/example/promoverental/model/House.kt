package com.example.promoverental.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class House(
    val id: String? = null,
    val title: String = "",
    val location: String = "",
    val price: String = "",
    val bedrooms: Int = 0,
    val bathrooms: Int = 0,
    val area: String = "",
    val description: String = "",
    @SerialName("image_url") val imageUrl: String = "", // ডাটাবেসের snake_case এর সাথে কানেক্ট করবে
    @SerialName("owner_id") val ownerId: String = "",   // ডাটাবেসের snake_case এর সাথে কানেক্ট করবে
    val type: String = "Apartment",
    val latitude: Double = 23.8103,
    val longitude: Double = 90.4125
) : java.io.Serializable
