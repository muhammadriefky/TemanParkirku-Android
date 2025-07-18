package com.example.projectskripsi.model

data class StatistikResponse(
    val success: Boolean,
    val data: StatistikData?
)

data class StatistikData(
    val total_parkir: Int,
    val parkir_aktif: Int
)
data class PendapatanResponse(
    val success: Boolean,
    val total_pendapatan: Int
)

data class StatistikRespon(
    val status: String,
    val data: StatistikDataPengguna
)

data class StatistikDataPengguna(
    val total_parkir: Int,
    val total_bayar: Int
)