package com.example.projectskripsi.Pelanggan

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectskripsi.R
import com.example.projectskripsi.config.NetworkConfig
import com.example.projectskripsi.config.SessionLogin
import com.example.projectskripsi.model.RiwayatResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class riwayat_pembayaran : AppCompatActivity() {
    private lateinit var rvHistory: RecyclerView
    private lateinit var tvNotFound: TextView
    private lateinit var historyAdapter: rv_riwayat_pembayaran

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_riwayat_pembayaran)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main1)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        rvHistory = findViewById(R.id.rvHistory)
        tvNotFound = findViewById(R.id.tvNotFound1)

        val session = SessionLogin(this)
        val userId = session.getUserId()

        loadRiwayat(userId)
    }

    override fun onResume() {
        super.onResume()
        val session = SessionLogin(this)
        val userId = session.getUserId()
        loadRiwayat(userId)
    }


    private fun loadRiwayat(userId: Int) {
        NetworkConfig.getServices().getRiwayatByUserId(userId).enqueue(object : Callback<List<RiwayatResponse>> {
            override fun onResponse(
                call: Call<List<RiwayatResponse>>,
                response: Response<List<RiwayatResponse>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val historyData = response.body()!!
                    historyAdapter = rv_riwayat_pembayaran(historyData)
                    rvHistory.layoutManager = LinearLayoutManager(this@riwayat_pembayaran)
                    rvHistory.adapter = historyAdapter

                    if (historyData.isEmpty()) {
                        tvNotFound.visibility = View.VISIBLE
                        rvHistory.visibility = View.GONE
                    } else {
                        tvNotFound.visibility = View.GONE
                        rvHistory.visibility = View.VISIBLE
                    }
                } else {
                    Toast.makeText(this@riwayat_pembayaran, "Gagal mengambil data riwayat", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<RiwayatResponse>>, t: Throwable) {
                Toast.makeText(this@riwayat_pembayaran, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
