package com.example.projectskripsi.model

data class UpdateResponse(
    val status: String,
    val message: String
)

data class PenggunaUpdateRequest(
    val nama: String,
    val no_hp: String,
    val jenis_kelamin: String,
    val alamat: String
)