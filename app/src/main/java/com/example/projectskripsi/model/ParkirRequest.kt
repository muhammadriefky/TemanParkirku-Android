package com.example.projectskripsi.model

data class ParkirRequest(
    val no_plat: String,
    val jam_masuk: String,
    val jenis_kendaraan: String,
    val tarif: Int,
    val order_id : String
)
