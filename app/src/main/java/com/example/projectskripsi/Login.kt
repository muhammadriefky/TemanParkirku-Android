package com.example.projectskripsi

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.projectskripsi.Pelanggan.Home_pelanggan
import com.example.projectskripsi.Penjaga.Home_penjaga
import com.example.projectskripsi.config.ApiServices
import com.example.projectskripsi.config.NetworkConfig
import com.example.projectskripsi.config.SessionLogin
import com.example.projectskripsi.databinding.ActivityLoginBinding
import com.example.projectskripsi.model.LoginResponse
import com.example.projectskripsi.model.UserRequest
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Login : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var apiService: ApiServices
    private lateinit var sessionManager: SessionLogin

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        apiService = NetworkConfig.getServices()
        sessionManager = SessionLogin(applicationContext)

        binding.txtRegister.setOnClickListener {
            startActivity(Intent(this, Register::class.java))
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.edText1.text.toString()
            val password = binding.edText2.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email & Password wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = UserRequest(email, password)

            apiService.login(request).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val loginResponse = response.body()!!
                        val user = loginResponse.user
                        val token = loginResponse.token

                        // Simpan sesi login
                        sessionManager.saveToken(token)
                        sessionManager.saveEmail(email)
                        sessionManager.saveUserId(user.id)
                        sessionManager.setUser(user)

                        // Cek role dan navigasi
                        when (user.role) {
                            "penjaga" -> {
                                startActivity(Intent(this@Login, Admin2::class.java))
                                finish()
                            }
                            "pengguna" -> {
                                startActivity(Intent(this@Login, Admin::class.java))
                                finish()
                            }
                            else -> {
                                Toast.makeText(this@Login, "Role tidak dikenal", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        val error = JSONObject(response.errorBody()?.string() ?: "{}")
                        val message = error.optString("message", "Login gagal")
                        Toast.makeText(this@Login, message, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Toast.makeText(this@Login, "Login error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
