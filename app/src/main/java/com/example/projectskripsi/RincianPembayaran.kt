package com.example.projectskripsi

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.example.projectskripsi.Pelanggan.PaymentActivity
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil

class RincianPembayaran : AppCompatActivity() {
    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rincian_pembayaran)

        val data = intent.data

        val noPlat = data?.getQueryParameter("no_plat")
        val waktuMasuk = data?.getQueryParameter("waktu")
        val tarif = data?.getQueryParameter("tarif")?.toIntOrNull() ?: 0
        val orderId = data?.getQueryParameter("order_id")

        findViewById<TextView>(R.id.noPlat).text = noPlat ?: "-"
        findViewById<TextView>(R.id.waktuMulai).text = waktuMasuk ?: "-"
        findViewById<TextView>(R.id.tarifParkir).text = "Rp $tarif / jam"

        // Format jam_masuk di-backend misal: "14:30"
        val fullFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val now = Date() // waktu sekarang

        var durasiMenit = 0
        var durasiJam = 0
        var biaya = 0

        waktuMasuk?.let {
            try {
                val waktuMasukDate = fullFormat.parse(waktuMasuk) // tanpa menambahkan todayDate lagi

                waktuMasukDate?.let { masuk ->
                    val selisihMs = now.time - masuk.time
                    durasiMenit = (selisihMs / (60 * 1000)).toInt()
                    val durasiJam = ceil(durasiMenit / 60.0).toInt().coerceAtLeast(1)
                    biaya = durasiJam * tarif

                    findViewById<TextView>(R.id.totalDurasi).text = "$durasiMenit Menit ($durasiJam Jam)"
                    findViewById<TextView>(R.id.totalBayar).text = "Rp $biaya"
                }
            } catch (e: Exception) {
                findViewById<TextView>(R.id.totalDurasi).text = "Format waktu tidak valid"
                findViewById<TextView>(R.id.totalBayar).text = "-"
            }
        }



        val btnBayar = findViewById<AppCompatButton>(R.id.btnBayar)
        btnBayar.setOnClickListener {
            val intent = Intent(this, PaymentActivity::class.java).apply {
                putExtra("NO_PLAT", noPlat)
                putExtra("WAKTU_MASUK", waktuMasuk)
                putExtra("TOTAL_DURASI", durasiJam.toString())
                putExtra("TARIF", tarif.toString())
                putExtra("TOTAL_BAYAR", biaya.toString())
                putExtra("ORDER_ID", orderId)
            }
            startActivity(intent)
        }
        Log.d("RincianPembayaran", "orderId: $orderId")
    }

}
