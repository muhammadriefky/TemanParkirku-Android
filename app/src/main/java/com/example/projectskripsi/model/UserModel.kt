package com.example.projectskripsi.model

data class UserRespon(
    val status: String,
    val data: PenggunaData
)

data class PenggunaData(
    val id: Int,
    val user_id: Int,
    val jenis_kelamin: String,
    val alamat: String,
    val plat_no: String?, // tambahkan sesuai JSON
    val no_hp: String,
    val last_payment_method: String?, // tambahkan
    val foto_url: String?,
    val user: UserData?
)

data class UserData(
    val id: Int,
    val nama: String,
    val email: String,
    val role: String
)

