package com.example.projectskripsi.model

data class RiwayatResponse(
    val user_id: Int,
    val tanggal: String,
    val nominal: String,
    val status: String,
    val metode: String,
    val order_id: String,
    val created_at: String // atau updated_at kalau itu yang tersedia
)

data class RiwayatResponseBody(
    val user_id: Int,
    val tanggal: String,
    val nominal: String,
    val status: String,
    val metode: String,
    val order_id: String
)