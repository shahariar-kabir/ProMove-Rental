package com.example.ptomoverental.model

import java.io.Serializable

data class MovingProvider(
    val id: String = "",
    val name: String = "",
    val rating: Float = 0.0f,
    val reviews: Int = 0,
    val pricePerKm: String = "",
    val description: String = "",
    val imageUrl: String = ""
) : Serializable