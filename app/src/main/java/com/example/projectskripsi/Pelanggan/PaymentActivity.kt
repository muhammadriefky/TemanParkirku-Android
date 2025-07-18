package com.example.projectskripsi.Pelanggan

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.projectskripsi.Admin
import com.example.projectskripsi.R
import com.example.projectskripsi.config.NetworkConfig
import com.example.projectskripsi.config.SessionLogin
import com.example.projectskripsi.model.LastPaymentResponse
import com.example.projectskripsi.model.RiwayatResponse
import com.example.projectskripsi.model.GeneralResponse
import com.example.projectskripsi.model.RiwayatResponseBody
import com.midtrans.sdk.corekit.callback.TransactionFinishedCallback
import com.midtrans.sdk.corekit.core.MidtransSDK
import com.midtrans.sdk.corekit.core.TransactionRequest
import com.midtrans.sdk.corekit.models.BillingAddress
import com.midtrans.sdk.corekit.models.CustomerDetails
import com.midtrans.sdk.corekit.models.ItemDetails
import com.midtrans.sdk.corekit.models.ShippingAddress
import com.midtrans.sdk.corekit.models.snap.TransactionResult
import com.midtrans.sdk.uikit.SdkUIFlowBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PaymentActivity : AppCompatActivity(), TransactionFinishedCallback {

    private lateinit var orderId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        initMidtransSDK()

        orderId = intent.getStringExtra("ORDER_ID") ?: ""
        if (orderId.isEmpty()) {
            Toast.makeText(this, "Order ID tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        getSnapTokenAndStart()
    }

    private fun initMidtransSDK() {
        SdkUIFlowBuilder.init()
            .setClientKey("SB-Mid-client-WdmpyaLSKvcMQDoo")
            .setContext(this)
            .setTransactionFinishedCallback(this)
            .setMerchantBaseUrl("http://192.168.1.6/midtrans-server/index.php/")
            .enableLog(true)
            .buildSDK()
    }

    private fun getSnapTokenAndStart() {
        val sessionManager = SessionLogin(this)
        val token = sessionManager.getToken()
        val totalBayar = intent.getStringExtra("TOTAL_BAYAR")?.toIntOrNull() ?: 0

        if (token.isNullOrEmpty() || orderId.isEmpty()) {
            Toast.makeText(this, "Data tidak lengkap", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val authHeader = "Bearer $token"

        val requestBody = mapOf(
            "order_id" to orderId,
            "total_bayar" to totalBayar
        )

        NetworkConfig.getServices().getSnapToken(authHeader, requestBody)
            .enqueue(object : Callback<LastPaymentResponse> {
                override fun onResponse(call: Call<LastPaymentResponse>, response: Response<LastPaymentResponse>) {
                    if (response.isSuccessful) {
                        val snapToken = response.body()?.snap_token
                        if (!snapToken.isNullOrEmpty()) {
                            MidtransSDK.getInstance().startPaymentUiFlow(this@PaymentActivity, snapToken)
                        } else {
                            Toast.makeText(this@PaymentActivity, "Snap token kosong", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    } else {
                        Toast.makeText(this@PaymentActivity, "Gagal ambil snap token", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }

                override fun onFailure(call: Call<LastPaymentResponse>, t: Throwable) {
                    Toast.makeText(this@PaymentActivity, "Gagal koneksi ke server", Toast.LENGTH_SHORT).show()
                    finish()
                }
            })
    }

    override fun onTransactionFinished(result: TransactionResult?) {
        val totalBayar = intent.getStringExtra("TOTAL_BAYAR")?.toIntOrNull() ?: 0

        if (result?.response != null) {
            val status = result.status
            val session = SessionLogin(this)
            val userId = session.getUserId()
            val tanggal = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())
            val paymentType = result.response?.paymentType ?: "Tidak diketahui"
            val nominal = totalBayar.toString()

            val statusRiwayat = when (status) {
                TransactionResult.STATUS_SUCCESS -> "Selesai"
                TransactionResult.STATUS_PENDING -> "Selesai"
                TransactionResult.STATUS_FAILED -> "Gagal"
                else -> "Tidak diketahui"
            }

            val riwayat = RiwayatResponseBody(
                user_id = userId,
                tanggal = tanggal,
                nominal = nominal,
                status = statusRiwayat,
                metode = paymentType,
                order_id = orderId
            )

            // Proses berdasarkan status transaksi
            when (status) {
                TransactionResult.STATUS_SUCCESS -> {
                    handleSuccessfulPayment(riwayat, paymentType, totalBayar)
                }
                TransactionResult.STATUS_PENDING -> {
                    handlePendingPayment(riwayat, paymentType, totalBayar)
                }
                TransactionResult.STATUS_FAILED -> {
                    handleFailedPayment(riwayat)
                }
                else -> {
                    Toast.makeText(this, "Status tidak diketahui", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

        } else if (result?.isTransactionCanceled == true) {
            Toast.makeText(this, "Pembayaran dibatalkan.", Toast.LENGTH_LONG).show()
            finish()
        } else {
            Toast.makeText(this, "Terjadi masalah pada pembayaran.", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun handleSuccessfulPayment(riwayat: RiwayatResponseBody, paymentType: String, totalBayar: Int) {
        NetworkConfig.getServices().simpanRiwayat(riwayat).enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                updatePaymentMethod(paymentType, totalBayar)
            }
            override fun onFailure(call: Call<Any>, t: Throwable) {
                Toast.makeText(this@PaymentActivity, "Gagal simpan riwayat", Toast.LENGTH_SHORT).show()
                updatePaymentMethod(paymentType, totalBayar)
            }
        })
    }

    private fun handlePendingPayment(riwayat: RiwayatResponseBody, paymentType: String, totalBayar: Int) {
        // Simpan riwayat
        NetworkConfig.getServices().simpanRiwayat(riwayat).enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                updatePaymentMethod(paymentType, totalBayar)
            }
            override fun onFailure(call: Call<Any>, t: Throwable) {
                Toast.makeText(this@PaymentActivity, "Gagal simpan riwayat", Toast.LENGTH_SHORT).show()
                updatePaymentMethod(paymentType, totalBayar)
            }
        })
    }

    private fun handleFailedPayment(riwayat: RiwayatResponseBody) {
        // Simpan riwayat pembayaran gagal
        NetworkConfig.getServices().simpanRiwayat(riwayat).enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                Toast.makeText(this@PaymentActivity, "Pembayaran gagal.", Toast.LENGTH_LONG).show()
                finish()
            }
            override fun onFailure(call: Call<Any>, t: Throwable) {
                Toast.makeText(this@PaymentActivity, "Pembayaran gagal.", Toast.LENGTH_LONG).show()
                finish()
            }
        })
    }

    private fun updatePaymentMethod(paymentType: String, totalBayar: Int) {
        val token = "Bearer ${SessionLogin(this).getToken()}"
        NetworkConfig.getServices().updateLastPaymentMethod(token, paymentType)
            .enqueue(object : Callback<GeneralResponse> {
                override fun onResponse(call: Call<GeneralResponse>, response: Response<GeneralResponse>) {
                    checkPaymentStatus(token, totalBayar)
                }
                override fun onFailure(call: Call<GeneralResponse>, t: Throwable) {
                    checkPaymentStatus(token, totalBayar)
                }
            })
    }

    private fun checkPaymentStatus(token: String, totalBayar: Int) {
        NetworkConfig.getServices().getStatusPembayaran(token, orderId)
            .enqueue(object : Callback<LastPaymentResponse> {
                override fun onResponse(call: Call<LastPaymentResponse>, response: Response<LastPaymentResponse>) {
                    if (response.isSuccessful) {
                        val data = response.body()
                        if (data?.status == "sudah_bayar") {
                            Toast.makeText(this@PaymentActivity, "Pembayaran berhasil terkonfirmasi!", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this@PaymentActivity, "Pembayaran berhasil!", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(this@PaymentActivity, "Pembayaran berhasil!", Toast.LENGTH_LONG).show()
                    }
                    redirectToPaymentDetails(totalBayar)
                }

                override fun onFailure(call: Call<LastPaymentResponse>, t: Throwable) {
                    Toast.makeText(this@PaymentActivity, "Pembayaran berhasil!", Toast.LENGTH_LONG).show()
                    redirectToPaymentDetails(totalBayar)
                }
            })
    }

    private fun redirectToPaymentDetails(totalBayar: Int) {
        // Redirect ke halaman rincian pembayaran
        val intent = Intent(this, Admin::class.java) // Ganti dengan activity yang sesuai
        intent.putExtra("ORDER_ID", orderId)
        intent.putExtra("TOTAL_BAYAR", totalBayar.toString())
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cleanup Midtrans SDK
        try {
        } catch (e: Exception) {
            // Ignore error
        }
    }
}