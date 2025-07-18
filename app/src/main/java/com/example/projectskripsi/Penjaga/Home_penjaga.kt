package com.example.projectskripsi.Penjaga

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.projectskripsi.R
import com.example.projectskripsi.config.ApiServices
import com.example.projectskripsi.config.NetworkConfig
import com.example.projectskripsi.config.SessionLogin
import com.example.projectskripsi.databinding.FragmentHomePenjagaBinding
import com.example.projectskripsi.model.PendapatanResponse
import com.example.projectskripsi.model.StatistikResponse
import com.example.projectskripsi.model.UserRespon
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale

class Home_penjaga : Fragment() {
    private lateinit var binding: FragmentHomePenjagaBinding
    private lateinit var session: SessionLogin
    private lateinit var apiService: ApiServices

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomePenjagaBinding.inflate(inflater, container, false)
        session = SessionLogin(requireContext())
        apiService = NetworkConfig.getServices()

        setupClickListeners()
        fetchUserData()
        loadTodayStatistics()

        return binding.root
    }

    private fun setupClickListeners() {
        // Menu Tambah Parkir
        binding.cardTambah.setOnClickListener {
            val intent = Intent(activity, TambahParkir::class.java)
            startActivity(intent)
        }

        // Menu Lihat Parkir Aktif
        binding.cardLihat.setOnClickListener {
            val intent = Intent(activity, Lihat_parkir::class.java)
            startActivity(intent)
        }

        // Notification bell click (optional)
        binding.headerCard.findViewById<androidx.cardview.widget.CardView>(R.id.headerCard)
            ?.findViewById<androidx.cardview.widget.CardView>(R.id.headerCard)
            ?.let { headerLayout ->
                // Find notification bell in header and set click listener
                // This is optional - you can add notification functionality here
            }
    }

    private fun fetchUserData() {
        val token = "Bearer ${session.getToken()}"
        apiService.getUserr(token).enqueue(object : Callback<UserRespon> {
            override fun onResponse(call: Call<UserRespon>, response: Response<UserRespon>) {
                if (response.isSuccessful) {
                    val user = response.body()?.data
                    if (user != null) {
                        // Update UI dengan data user
                        binding.txtNamaPenjaga.text = user.user?.nama ?: "Nama Tidak Tersedia"
                        binding.txtNotelpPenjaga.text = user.no_hp ?: "No HP Tidak Tersedia"
                        binding.txtAddressPenjaga.text = user.alamat ?: "Alamat Tidak Tersedia"

                        // Load profile image dari tabel pengguna (bukan dari user)
                        loadProfileImage(user.foto_url) // Menggunakan user.foto langsung dari tabel pengguna
                    }
                } else {
                    Toast.makeText(context, "Gagal mengambil data pengguna", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserRespon>, t: Throwable) {
                Toast.makeText(context, "Terjadi kesalahan: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadProfileImage(imageData: String?) {
        val profileImageView = binding.profileCard.findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.profileImage)

        when {
            // Jika ada URL gambar dari server
            imageData?.startsWith("http") == true -> {
                loadImageFromUrl(imageData, profileImageView)
            }
            // Jika ada data base64
            imageData?.isNotEmpty() == true -> {
                loadImageFromBase64(imageData, profileImageView)
            }
            // Jika tidak ada gambar, gunakan default
            else -> {
                setDefaultProfileImage(profileImageView)
            }
        }
    }

    private fun loadImageFromUrl(imageUrl: String, imageView: de.hdodenhof.circleimageview.CircleImageView) {
        try {
            Glide.with(this)
                .load(imageUrl)
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.ic_person) // Gambar placeholder
                        .error(R.drawable.ic_person) // Gambar error - diperbaiki dari referensi midtrans
                        .centerCrop()
                )
                .into(imageView)
        } catch (e: Exception) {
            setDefaultProfileImage(imageView)
        }
    }

    private fun loadImageFromBase64(base64String: String, imageView: de.hdodenhof.circleimageview.CircleImageView) {
        try {
            // Hapus prefix jika ada (contoh: "data:image/jpeg;base64,")
            val cleanBase64 = if (base64String.contains(",")) {
                base64String.substring(base64String.indexOf(",") + 1)
            } else {
                base64String
            }

            val decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

            if (bitmap != null) {
                imageView.setImageBitmap(bitmap)
            } else {
                setDefaultProfileImage(imageView)
            }
        } catch (e: Exception) {
            setDefaultProfileImage(imageView)
        }
    }

    private fun setDefaultProfileImage(imageView: de.hdodenhof.circleimageview.CircleImageView) {
        // Set gambar default
        imageView.setImageResource(R.drawable.ic_person)

        // Atau bisa menggunakan gambar avatar default
        // imageView.setImageResource(R.drawable.default_avatar)
    }

    private fun loadTodayStatistics() {
        val token = "Bearer ${session.getToken()}"
        apiService.getStatistikPenjaga(token).enqueue(object : Callback<StatistikResponse> {
            override fun onResponse(
                call: Call<StatistikResponse>,
                response: Response<StatistikResponse>
            ) {
                if (response.isSuccessful) {
                    val data = response.body()?.data
                    val totalParkir = data?.total_parkir ?: 0
                    val parkirAktif = data?.parkir_aktif ?: 0
                    fetchPendapatanHariIni(totalParkir, parkirAktif)
                } else {
                    setDefaultStatistics()
                }
            }

            override fun onFailure(call: Call<StatistikResponse>, t: Throwable) {
                setDefaultStatistics()
            }
        })
    }

    private fun setDefaultStatistics() {
        updateStatisticsDisplay(
            totalParkir = 24,
            sedangAktif = 8,
            pendapatan = "Rp 120k"
        )
    }

    private fun fetchPendapatanHariIni(total: Int, aktif: Int) {
        val token = "Bearer ${session.getToken()}"

        apiService.getPendapatanHariIni(token).enqueue(object : Callback<PendapatanResponse> {
            override fun onResponse(call: Call<PendapatanResponse>, response: Response<PendapatanResponse>) {
                if (response.isSuccessful) {
                    val pendapatan = response.body()?.total_pendapatan ?: 0
                    updateStatisticsDisplay(
                        totalParkir = total,
                        sedangAktif = aktif,
                        pendapatan = formatRupiah(pendapatan)
                    )
                } else {
                    updateStatisticsDisplay(
                        totalParkir = total,
                        sedangAktif = aktif,
                        pendapatan = "Rp -"
                    )
                }
            }

            override fun onFailure(call: Call<PendapatanResponse>, t: Throwable) {
                updateStatisticsDisplay(
                    totalParkir = total,
                    sedangAktif = aktif,
                    pendapatan = "Rp -"
                )
            }
        })
    }

    private fun formatRupiah(nominal: Int): String {
        val localeID = Locale("in", "ID")
        val format = NumberFormat.getCurrencyInstance(localeID)
        return format.format(nominal).replace(",00", "")
    }

    private fun updateStatisticsDisplay(totalParkir: Int, sedangAktif: Int, pendapatan: String) {
        binding.txtTotalParkir.text = totalParkir.toString()
        binding.txtParkirAktif.text = sedangAktif.toString()
        binding.txtPendapatan.text = pendapatan
    }

    override fun onResume() {
        super.onResume()
        fetchUserData()
        loadTodayStatistics()
    }

    fun refreshData() {
        fetchUserData()
        loadTodayStatistics()
    }
}