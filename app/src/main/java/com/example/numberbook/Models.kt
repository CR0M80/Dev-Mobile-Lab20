package com.example.numberbook

import com.google.gson.annotations.SerializedName

data class Contact(
    val id: Int? = null,
    val name: String,
    val phone: String,
    val source: String = "mobile",
    @SerializedName("created_at") val createdAt: String? = null
)

data class ApiResponse(
    val success: Boolean,
    val message: String
)
