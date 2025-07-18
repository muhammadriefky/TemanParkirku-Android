package com.example.projectskripsi.Penjaga

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.projectskripsi.R
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.net.URLEncoder

class GenerateQrcode : AppCompatActivity() {
    private lateinit var imgQrCode: ImageView
    private lateinit var tvStatus: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generate_qrcode)

        imgQrCode = findViewById(R.id.imgQrCode)
        tvStatus = findViewById(R.id.tvStatus)

        val noPlat = intent.getStringExtra("NO_PLAT") ?: "UNKNOWN"
        val waktuMasuk = intent.getStringExtra("WAKTU_MASUK") ?: "UNKNOWN"
        val tarif = intent.getStringExtra("TARIF")?.toIntOrNull() ?: 5000
        val orderId = intent.getStringExtra("ORDER_ID") ?: "UNKNOWN"

        val waktuEncoded = URLEncoder.encode(waktuMasuk, "UTF-8")
        val orderIdEncoded = URLEncoder.encode(orderId, "UTF-8")

        // 🧠 Tambahkan order_id ke URL QR
        val qrData = "myapp://rincian?no_plat=$noPlat&waktu=$waktuEncoded&tarif=$tarif&order_id=$orderIdEncoded"

        val qrBitmap = generateQrCode(qrData)
        imgQrCode.setImageBitmap(qrBitmap)

        tvStatus.text = "QR untuk $noPlat berhasil dibuat"
    }

    private fun generateQrCode(text: String): Bitmap {
        val writer = QRCodeWriter()
        val matrix = writer.encode(text, BarcodeFormat.QR_CODE, 512, 512)
        val bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565)

        for (x in 0 until 512) {
            for (y in 0 until 512) {
                bitmap.setPixel(x, y, if (matrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }
}
