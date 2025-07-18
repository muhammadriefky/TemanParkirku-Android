package com.example.projectskripsi.Pelanggan

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.projectskripsi.R
import com.google.zxing.ResultPoint
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView


class scanQR_pelanggan : AppCompatActivity() {
    private lateinit var barcodeView: DecoratedBarcodeView
    private lateinit var imgFlash: ImageView
    private var isFlashOn: Boolean = false
    private val PICK_IMAGE_REQUEST = 1


    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 100
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_qr_pelanggan) // Pastikan nama layout sesuai

        // Inisialisasi komponen UI
        // Pastikan R.id.zxing_barcode_scanner ada di activity_scan_qr_pelanggan.xml
        barcodeView = findViewById(R.id.zxing_barcode_scanner)
        imgFlash = findViewById(R.id.imgFlash)
        val imgGaleri: ImageView = findViewById(R.id.imgGaleri)

        // Request izin kamera
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        } else {
            // Izin sudah diberikan, mulai inisialisasi scanner
            setupScanner()
        }

        // Setup listener untuk tombol flash
        imgFlash.setOnClickListener {
            toggleFlash()
        }

        // Setup listener untuk tombol galeri (opsional, bisa ditambahkan logika untuk memilih gambar dari galeri)
        imgGaleri.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent,PICK_IMAGE_REQUEST)
        }
    }

    private fun setupScanner() {
        // Mengatur opsi pemindaian pada DecoratedBarcodeView
        barcodeView.barcodeView.cameraSettings.isAutoFocusEnabled = true // Aktifkan autofokus
        barcodeView.barcodeView.cameraSettings.isContinuousFocusEnabled = true // Aktifkan fokus berkelanjutan
        barcodeView.barcodeView.cameraSettings.requestedCameraId = 0 // Gunakan kamera belakang

        // Mulai memindai secara terus-menerus
        barcodeView.decodeContinuous(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult) {
                val scannedData = result.text
                if (scannedData.startsWith("myapp://rincian")) {
                    val uri = Uri.parse(scannedData)
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                    finish()
                }
            }


            override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) {
                // Callback untuk menampilkan poin-poin hasil yang mungkin
                // Tidak perlu banyak dilakukan di sini untuk kasus sederhana
            }
        })
    }

    override fun onResume() {
        super.onResume()
        // Melanjutkan pemindaian saat activity dilanjutkan
        barcodeView.resume()
    }

    override fun onPause() {
        super.onPause()
        // Menghentikan pemindaian saat activity di-pause
        barcodeView.pause()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupScanner()
            } else {
                Toast.makeText(this, "Izin kamera diperlukan untuk memindai QR Code", Toast.LENGTH_LONG).show()
                finish() // Tutup activity jika izin tidak diberikan
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            val imageUri = data.data
            imageUri?.let {
                decodeQRCodeFromGallery(it)
            }
        }
    }

    private fun decodeQRCodeFromGallery(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)

            val intArray = IntArray(bitmap.width * bitmap.height)
            bitmap.getPixels(intArray, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

            val source = com.google.zxing.RGBLuminanceSource(bitmap.width, bitmap.height, intArray)
            val binaryBitmap = com.google.zxing.BinaryBitmap(com.google.zxing.common.HybridBinarizer(source))

            val reader = com.google.zxing.MultiFormatReader()
            val result = reader.decode(binaryBitmap)

            val scannedData = result.text
            if (scannedData.startsWith("myapp://rincian")) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(scannedData))
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "QR Code tidak dikenali!", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Gagal membaca QR dari gambar", Toast.LENGTH_SHORT).show()
        }
    }


    private fun toggleFlash() {
        // Cek apakah kamera aktif sebelum mencoba mengubah flash
        if (barcodeView.barcodeView.isPreviewActive) {
            isFlashOn = !isFlashOn
            barcodeView.setTorchOn() // Mengubah status flash
            if (isFlashOn) {
                imgFlash.setImageResource(R.drawable.ic_flash) // Pastikan Anda memiliki icon untuk flash menyala
                Toast.makeText(this, "Flash menyala", Toast.LENGTH_SHORT).show()
            } else {
                imgFlash.setImageResource(R.drawable.ic_flash) // Kembali ke icon flash mati
                Toast.makeText(this, "Flash mati", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Kamera tidak aktif atau sedang dimuat.", Toast.LENGTH_SHORT).show()
        }
    }
}