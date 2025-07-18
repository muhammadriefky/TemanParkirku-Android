package com.example.projectskripsi.Pelanggan

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.projectskripsi.R
import com.example.projectskripsi.config.ApiServices
import com.example.projectskripsi.config.NetworkConfig
import com.example.projectskripsi.config.SessionLogin
import com.example.projectskripsi.databinding.ActivityLihatParkirBinding
import com.example.projectskripsi.databinding.ActivityScanQrPelangganBinding
import com.example.projectskripsi.databinding.FragmentHomePelangganBinding
import com.example.projectskripsi.model.UserRespon
import com.example.projectskripsi.model.PenggunaData
import com.example.projectskripsi.model.RiwayatResponse
import com.example.projectskripsi.model.StatistikRespon
import com.example.projectskripsi.model.UserData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class Home_pelanggan : Fragment() {

    private lateinit var binding: FragmentHomePelangganBinding
    private lateinit var session: SessionLogin
    private lateinit var apiService: ApiServices

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomePelangganBinding.inflate(inflater, container, false)
        session = SessionLogin(requireContext())
        apiService = NetworkConfig.getServices()

        // Fetch user data saat fragment dimuat
        fetchUserData()
        fetchStatistik()
        fetchLastActivity()
        // Setup click listeners untuk menu cards
        setupClickListeners()

        return binding.root
    }

    private fun setupClickListeners() {
        // Click listener untuk menu Bayar Parkir (QR Scanner)
        binding.cardBayarParkir.setOnClickListener {
            val intent = Intent(activity, scanQR_pelanggan::class.java)
            startActivity(intent)
        }

        // Click listener untuk menu Riwayat Pembayaran
        binding.cardRiwayat.setOnClickListener {
            val intent = Intent(activity, riwayat_pembayaran::class.java)
            startActivity(intent)
        }

        // Click listener untuk profile image (opsional)
        binding.profileImage.setOnClickListener {
            // Handle profile image click - bisa untuk edit profile atau view profile
            Toast.makeText(context, "Profile menu", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchLastActivity() {
        val token = "Bearer ${session.getToken()}"
        apiService.getRiwayatByUserId(session.getUserId()).enqueue(object : Callback<List<RiwayatResponse>> {
            override fun onResponse(call: Call<List<RiwayatResponse>>, response: Response<List<RiwayatResponse>>) {
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    val last = response.body()!![0]
                    val displayStatus = when (last.status.lowercase()) {
                        "selesai" -> "Pembayaran Berhasil"
                        "gagal" -> "Pembayaran Gagal"
                        else -> "Status Tidak Diketahui"
                    }
                    binding.txtStatus.text = displayStatus // Contoh: "Pembayaran Berhasil"
                    binding.txtMetode.text = last.metode // Contoh: "ShopeePay"
                    binding.txtNominal.text = formatCurrency(last.nominal.toDouble())
                    binding.txtWaktu.text = getTimeAgo(last.created_at) // Misal "2 jam lalu"
                } else {
                    binding.txtStatus.text = "Belum ada aktivitas"
                }
            }

            override fun onFailure(call: Call<List<RiwayatResponse>>, t: Throwable) {
                binding.txtStatus.text = "Gagal memuat"
            }
        })
    }
    private fun getTimeAgo(time: String): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return try {
            val past = sdf.parse(time)
            val now = Date()
            val seconds = (now.time - past.time) / 1000
            val minutes = seconds / 60
            val hours = minutes / 60
            val days = hours / 24
            when {
                seconds < 60 -> "Baru saja"
                minutes < 60 -> "$minutes menit lalu"
                hours < 24 -> "$hours jam lalu"
                else -> "$days hari lalu"
            }
        } catch (e: Exception) {
            "-"
        }
    }



    private fun fetchStatistik() {
        val token = "Bearer ${session.getToken()}"

        apiService.getStatistik(token).enqueue(object : Callback<StatistikRespon> {
            override fun onResponse(call: Call<StatistikRespon>, response: Response<StatistikRespon>) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    val data = response.body()!!.data

                    binding.txtTotalParkir.text = data.total_parkir.toString()
                    binding.txtTotalBayar.text = formatCurrency(data.total_bayar.toDouble())
                } else {
                    // Gagal ambil data, tampilkan default
                    binding.txtTotalParkir.text = "0"
                    binding.txtTotalBayar.text = "Rp 0"
                }
            }

            override fun onFailure(call: Call<StatistikRespon>, t: Throwable) {
                // Error koneksi
                binding.txtTotalParkir.text = "0"
                binding.txtTotalBayar.text = "Rp 0"
            }
        })
    }

    private fun fetchUserData() {
        val token = "Bearer ${session.getToken()}"

        // Tampilkan loading state (opsional)
        showLoadingState()

        apiService.getUser(token).enqueue(object : Callback<UserRespon> {
            override fun onResponse(call: Call<UserRespon>, response: Response<UserRespon>) {
                // Sembunyikan loading state
                hideLoadingState()

                if (response.isSuccessful) {
                    val userResponse = response.body()
                    if (userResponse != null) {
                        updateUserInterface(userResponse.data)
                    } else {
                        showErrorMessage("Data pengguna tidak ditemukan")
                    }
                } else {
                    val errorMessage = when (response.code()) {
                        401 -> "Sesi telah berakhir, silakan login kembali"
                        403 -> "Akses ditolak"
                        404 -> "Data pengguna tidak ditemukan"
                        500 -> "Server sedang bermasalah"
                        else -> "Gagal mengambil data pengguna (${response.code()})"
                    }
                    showErrorMessage(errorMessage)
                }
            }

            override fun onFailure(call: Call<UserRespon>, t: Throwable) {
                hideLoadingState()
                val errorMessage = when {
                    t.message?.contains("timeout", true) == true -> "Koneksi timeout, coba lagi"
                    t.message?.contains("network", true) == true -> "Tidak ada koneksi internet"
                    else -> "Terjadi kesalahan: ${t.message}"
                }
                showErrorMessage(errorMessage)
            }
        })
    }

    private fun updateUserInterface(penggunaData: PenggunaData) {
        try {
            // Update nama pengguna dari tabel pengguna (prioritas utama)
            // Jika nama di tabel pengguna kosong, gunakan nama dari tabel user
            val userName = if (penggunaData.user!!.nama.isNotEmpty()) {
                penggunaData.user!!.nama
            } else {
                penggunaData.user?.nama ?: "Nama Tidak Tersedia"
            }
            binding.txtNama.text = userName

            // Update nomor telepon dari tabel pengguna
            val phoneNumber = penggunaData.no_hp
            if (phoneNumber.isNotEmpty()) {
                binding.txtNotelp.text = phoneNumber
            } else {
                binding.txtNotelp.text = "Nomor tidak tersedia"
            }

            // Update email dari tabel user
            val userEmail = penggunaData.user?.email ?: "Email tidak tersedia"
            binding.txtAddress.text = userEmail

            // Update foto profil dari tabel pengguna
            loadProfileImage(penggunaData.foto_url)

            // Update data statistik jika ada
            updateStatisticsData(penggunaData)

        } catch (e: Exception) {
            showErrorMessage("Terjadi kesalahan saat menampilkan data: ${e.message}")
        }
    }

    private fun loadProfileImage(fotoPath: String?) {
        try {
            if (!fotoPath.isNullOrEmpty()) {
                // Jika menggunakan Glide untuk load image
                Glide.with(this)
                    .load(fotoPath) // atau build full URL jika perlu: "https://yourserver.com/uploads/$fotoPath"
                    .placeholder(R.drawable.logo_parkir) // placeholder saat loading
                    .error(R.drawable.logo_parkir) // gambar default jika error
                    .circleCrop() // untuk membuat gambar circular
                    .into(binding.profileImage)
            } else {
                // Gunakan gambar default jika tidak ada foto
                binding.profileImage.setImageResource(R.drawable.logo_parkir)
            }
        } catch (e: Exception) {
            // Fallback ke gambar default jika terjadi error
            binding.profileImage.setImageResource(R.drawable.logo_parkir)
        }
    }

    private fun updateStatisticsData(penggunaData: PenggunaData) {
        // Update statistik parkir jika data tersedia
        // Contoh: jika ada field total_parkir dan total_bayar di response
        /*
        penggunaData.totalParkir?.let { total ->
            binding.root.findViewById<TextView>(R.id.txtTotalParkir)?.text = total.toString()
        }

        penggunaData.totalBayar?.let { total ->
            binding.root.findViewById<TextView>(R.id.txtTotalBayar)?.text = formatCurrency(total)
        }
        */

        // Untuk sementara, kita bisa menampilkan info dasar
        // Misalnya status berdasarkan ketersediaan data
        val statusText = if (penggunaData.user != null) "Aktif" else "Tidak Aktif"
        // Update status di UI jika diperlukan
    }

    private fun showLoadingState() {
        // Tampilkan loading indicator atau disable UI
        binding.txtNama.text = "Memuat..."
        binding.txtNotelp.text = "Memuat..."
        binding.txtAddress.text = "Memuat..."
    }

    private fun hideLoadingState() {
        // Sembunyikan loading indicator
        // Implementasi tergantung pada UI loading yang digunakan
    }

    private fun showErrorMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()

        // Set default values jika terjadi error
        binding.txtNama.text = "Pengguna"
        binding.txtNotelp.text = "Nomor tidak tersedia"
        binding.txtAddress.text = "Email tidak tersedia"
    }

    private fun formatCurrency(amount: Double): String {
        return "Rp ${String.format("%,.0f", amount)}"
    }

    override fun onResume() {
        super.onResume()
        // Refresh data saat fragment kembali aktif
        fetchUserData()
    }
}