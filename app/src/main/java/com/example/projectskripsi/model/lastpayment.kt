package com.example.projectskripsi.model

data class LastPaymentResponse(
    val snap_token: String,
    val last_payment_method: String?,
    val status: String?
)


data class GeneralResponse(
    val status: String,
    val message: String
)
