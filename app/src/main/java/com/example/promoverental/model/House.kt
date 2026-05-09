package com.example.promoverental.model

import kotlinx.serialization.Serializable

@Serializable
data class House(
    val id: String = "",
    val title: String = "",
    val location: String = "",
    val price: String = "",
    val bedrooms: Int = 0,
    val bathrooms: Int = 0,
    val area: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val ownerId: String = "",
    val type: String = "Apartment", // e.g., Apartment, House, Room
    val latitude: Double = 23.8103, // Default to Dhaka coordinates
    val longitude: Double = 90.4125
) : java.io.Serializable
