package com.example.projectskripsi

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity

import com.example.projectskripsi.R
import com.example.projectskripsi.config.ApiServices
import com.example.projectskripsi.config.NetworkConfig
import com.example.projectskripsi.config.Regis
import com.example.projectskripsi.databinding.ActivityRegisterBinding
import com.example.projectskripsi.model.RegisterResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Register : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var ApiServices : ApiServices

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ApiServices = NetworkConfig.getServices()

        // Setup Spinner Role
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.role_array,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.txtRegister.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }

        binding.btnLogin.setOnClickListener {
            val nama = binding.edNamaRegister.text.toString()
            val email = binding.edEmailRegister.text.toString()
            val password = binding.edPasswordRegister.text.toString()
            val nomor_telepon = binding.edNoRegister.text.toString()
            val role = "pengguna"

            if (nama.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && role.isNotEmpty() && nomor_telepon.isNotEmpty()) {
                val regis = Regis(nama, email, password, role, nomor_telepon)
                ApiServices.register(regis).enqueue(object : Callback<RegisterResponse?> {
                    override fun onResponse(call: Call<RegisterResponse?>, response: Response<RegisterResponse?>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@Register, "Registration successful", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@Register, Login::class.java))
                            finish()
                        } else {
                            Toast.makeText(this@Register, "Pendaftaran Gagal", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<RegisterResponse?>, t: Throwable) {
                        Toast.makeText(this@Register, "An error occurred", Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                Toast.makeText(this@Register, "Tolong isi Semua Datanya", Toast.LENGTH_SHORT).show()
            }
        }
    }

}