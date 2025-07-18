package com.example.projectskripsi.model

data class UserResponse(
    val id: Int,
    val nama: String,
    val email: String,
    val role: String,
    val no_hp: String,
    val alamat: String,
    val token: String
)
