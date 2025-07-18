package com.example.projectskripsi.Penjaga

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.projectskripsi.R
import com.example.projectskripsi.config.NetworkConfig
import com.example.projectskripsi.model.ParkirRequest
import com.example.projectskripsi.model.ParkirResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class TambahParkir : AppCompatActivity() {
    private lateinit var edNoplat: EditText
    private lateinit var edJamMasuk: EditText
    private lateinit var spinnerJenis: Spinner
    private lateinit var edTarif: EditText
    private lateinit var btnTambah: Button
    val apiService = NetworkConfig.getServices()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_parkir)

        edNoplat = findViewById(R.id.edNoplat)
        edJamMasuk = findViewById(R.id.edTex2)
        spinnerJenis = findViewById(R.id.inputJnsKendaraan)
        edTarif = findViewById(R.id.edTex4)
        btnTambah = findViewById(R.id.buttonEdit)

        // Nonaktifkan input manual di EditText jam masuk
        edJamMasuk.inputType = android.text.InputType.TYPE_NULL
        edJamMasuk.isFocusable = false

        // Klik untuk menampilkan TimePicker
        edJamMasuk.setOnClickListener {
            showDateTimePicker()
        }

        // Isi Spinner
        val jenisKendaraan = arrayOf("Mobil", "Motor")
        val adapter = ArrayAdapter(this, R.layout.spinner_item_with_arrow, jenisKendaraan)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerJenis.adapter = adapter

        // Listener Spinner untuk mengisi otomatis tarif
        spinnerJenis.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (jenisKendaraan[position]) {
                    "Mobil" -> edTarif.setText("Rp 5.000")
                    "Motor" -> edTarif.setText("Rp 2.000")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                edTarif.setText("")
            }
        }

        // Tombol tambah
        btnTambah.setOnClickListener {
            val noplat = edNoplat.text.toString()
            val jamMasuk = edJamMasuk.text.toString()
            val jenis = spinnerJenis.selectedItem.toString()
            val tarifStr = edTarif.text.toString().replace("Rp ", "").replace(".", "")
            val tarif = tarifStr.toIntOrNull()

            if (noplat.isBlank() || jamMasuk.isBlank() || tarif == null) {
                Toast.makeText(this, "Harap isi semua data dengan benar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val orderId = "PARKIR-${System.currentTimeMillis()}"

            val parkir = ParkirRequest(noplat, jamMasuk, jenis, tarif, orderId)

            apiService.tambahParkir(parkir).enqueue(object : Callback<ParkirResponse> {
                override fun onResponse(
                    call: Call<ParkirResponse>,
                    response: Response<ParkirResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(
                            this@TambahParkir,
                            "Data berhasil ditambahkan",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    } else {
                        Toast.makeText(
                            this@TambahParkir,
                            "Gagal menambahkan data",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ParkirResponse>, t: Throwable) {
                    Toast.makeText(
                        this@TambahParkir,
                        "Error: ${t.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }
    }

        private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()

        val datePicker = DatePickerDialog(this,
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(Calendar.YEAR, year)
                selectedDate.set(Calendar.MONTH, month)
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                // Setelah tanggal dipilih, tampilkan TimePicker
                val timePicker = TimePickerDialog(this,
                    { _, hourOfDay, minute ->
                        selectedDate.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        selectedDate.set(Calendar.MINUTE, minute)

                        val formatted = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                            .format(selectedDate.time)

                        edJamMasuk.setText(formatted)
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true // format 24-jam
                )
                timePicker.show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

}
