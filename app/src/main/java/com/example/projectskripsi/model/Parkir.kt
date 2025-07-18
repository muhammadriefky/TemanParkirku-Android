package com.example.projectskripsi.model

data class Parkir(
    val id: Int,
    val no_plat: String,
    val jam_masuk: String,
    val jenis_kendaraan: String,
    val tarif: Int,
    val status: String, // ← penting!
    val order_id: String
)
