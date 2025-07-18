package com.example.projectskripsi.model

data class LoginResponse(
    val status: String,
    val message: String,
    val user: UserResponse,
    val token: String
)

