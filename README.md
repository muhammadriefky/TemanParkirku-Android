# 📱 TemanParkirku-Android

![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-orange)
![Gradle](https://img.shields.io/badge/Gradle-8.0-green)
![Platform](https://img.shields.io/badge/Platform-Android-lightgrey)
![License](https://img.shields.io/badge/License-MIT-yellow.svg)
![API](https://img.shields.io/badge/Backend-Laravel--API-blue)

TemanParkirku-Android adalah aplikasi mobile berbasis Kotlin yang menjadi antarmuka utama bagi pengguna dan petugas parkir dalam sistem retribusi parkir digital. Aplikasi ini terintegrasi penuh dengan backend Laravel REST API untuk proses autentikasi, pencatatan, dan pembayaran retribusi parkir.

---

## 📦 Fitur Utama

- ✅ Registrasi & login pengguna dan petugas
- 🚙 Pencatatan parkir masuk & keluar oleh petugas
- ⏱️ Perhitungan tarif otomatis berdasarkan durasi parkir
- 💳 Pembayaran retribusi melalui Midtrans (Snap)
- 🧾 Riwayat transaksi & detail status parkir
- 📱 Antarmuka Android responsif untuk pengguna dan petugas
- 🔐 Autentikasi token menggunakan Laravel Sanctum
- 👮‍♂️ Role management: Petugas & Pelanggan
- 🔍 Scan QR Code untuk verifikasi kendaraan

---

## 🧰 Teknologi yang Digunakan

- Kotlin (Jetpack)
- MVVM Architecture
- Retrofit (REST API Client)
- SharedPreferences (Token Storage)
- Glide (Image Loader)
- Firebase (opsional)

---

## 🔗 Terintegrasi Dengan

- [TemanParkirku-API (Laravel Backend)](https://github.com/MRiefkyR/TemanParkirku-API)

---

## 📂 Struktur Proyek (Ringkasan)

```bash
TemanParkirku-Android/
├── app/
│   └── src/
│       └── main/
│           ├── java/com/example/projectskripsi/
│           └── res/
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
└── README.md
```
🚀 Cara Menjalankan
1. Clone Repository
```bash
git clone https://github.com/MRiefkyR/TemanParkirku-Android.git
```
2. Buka Android Studio
3. Atur URL Retroift API di ApiServices.kt
```bash
val BASE_URL = "http://192.168.xx.xx:8000/api/"
```
4. Jalankan Aplikasi di Emulator atau Perangkat Fisik

⚠️ Catatan Developer

1. Pastikan Laravel API aktif di jaringan lokal atau hosting.
2. Gunakan token dari Laravel Sanctum pada setiap request API yang membutuhkan autentikasi.
3. Ganti BASE_URL sesuai dengan alamat IP server backend kamu.
