package com.example.projectskripsi.Penjaga

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.SearchView
import com.example.projectskripsi.R
import com.example.projectskripsi.config.NetworkConfig
import com.example.projectskripsi.model.Parkir
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Lihat_parkir : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LihatParkirAdapter
    private lateinit var tvNot: TextView
    private lateinit var searchView: SearchView
    private var dataParkir: List<Parkir> = listOf()
    private var filteredData: MutableList<Parkir> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lihat_parkir)

        recyclerView = findViewById(R.id.rvParkiraktif)
        tvNot = findViewById(R.id.tvNot)
        searchView = findViewById(R.id.searchView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Panggil API Laravel
        val api = NetworkConfig.getServices()
        api.getParkirBelumBayar().enqueue(object : Callback<List<Parkir>> {
            override fun onResponse(call: Call<List<Parkir>>, response: Response<List<Parkir>>) {
                if (response.isSuccessful) {
                    dataParkir = response.body() ?: listOf()
                    filteredData = dataParkir.toMutableList()
                    adapter = LihatParkirAdapter(this@Lihat_parkir, filteredData)
                    recyclerView.adapter = adapter
                    updateVisibility()
                } else {
                    tvNot.text = "Gagal mengambil data dari server"
                    updateVisibility()
                }
            }

            override fun onFailure(call: Call<List<Parkir>>, t: Throwable) {
                tvNot.text = "Error: ${t.message}"
                updateVisibility()
            }
        })

        // Search logic
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false

            override fun onQueryTextChange(newText: String?): Boolean {
                val input = newText.orEmpty().lowercase()
                filteredData.clear()
                filteredData.addAll(dataParkir.filter {
                    it.no_plat.lowercase().contains(input)
                })
                adapter.notifyDataSetChanged()
                updateVisibility()
                return true
            }
        })
    }

    private fun updateVisibility() {
        if (filteredData.isEmpty()) {
            tvNot.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            tvNot.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }
}
