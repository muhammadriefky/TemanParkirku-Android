package com.example.projectskripsi

data class PembayaranHistory(
    val date: String, // Tanggal transaksi
    val amount: String,
    val status: String,
    val paymentMethodName: String, // Nama metode pembayaran (misal: "Dana", "Gopay", "Bank Mandiri")
    val paymentMethodLogoResId: Int // Resource ID untuk logo metode pembayaran (misal: R.drawable.logo_dana)
)