package com.example.projectskripsi.Penjaga

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.animation.ObjectAnimator
import android.animation.AnimatorSet
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.widget.doOnTextChanged
import com.example.projectskripsi.Login
import com.example.projectskripsi.config.ApiServices
import com.example.projectskripsi.config.NetworkConfig
import com.example.projectskripsi.config.SessionLogin
import com.example.projectskripsi.databinding.FragmentSettingPenjagaBinding
import com.example.projectskripsi.model.UpdateResponse
import com.example.projectskripsi.model.UserRespon
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Import untuk foto
import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.projectskripsi.R
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class Setting_penjaga : Fragment() {

    private lateinit var edNama: EditText
    private lateinit var edNoHp: EditText
    private lateinit var edJenisKelamin: AutoCompleteTextView
    private lateinit var edAlamat: EditText
    private lateinit var btnEdit: Button
    private lateinit var btnCancel: Button
    private lateinit var btnBack: View
    private lateinit var binding: FragmentSettingPenjagaBinding
    private lateinit var sessionManager: SessionLogin
    private lateinit var session: SessionLogin
    private lateinit var apiService: ApiServices

    // Variables untuk foto
    private lateinit var imgProfile: ImageView
    private lateinit var btnChangePhoto: View
    private var currentPhotoPath: String = ""
    private var selectedImageUri: Uri? = null

    // Variables untuk menyimpan data original
    private var originalNama: String = ""
    private var originalNoHp: String = ""
    private var originalJenisKelamin: String = ""
    private var originalAlamat: String = ""
    private var originalFoto: String = ""
    private var isDataChanged: Boolean = false

    // Activity result launchers untuk foto
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            showImageSourceDialog()
        } else {
            Toast.makeText(requireContext(), "Permission diperlukan untuk mengakses kamera", Toast.LENGTH_SHORT).show()
        }
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess: Boolean ->
        if (isSuccess) {
            selectedImageUri?.let { uri ->
                loadImageToImageView(uri)
                checkDataChanged()
            }
        }
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            loadImageToImageView(it)
            checkDataChanged()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingPenjagaBinding.inflate(inflater, container, false)
        session = SessionLogin(requireContext())
        apiService = NetworkConfig.getServices()

        // Inisialisasi session
        sessionManager = SessionLogin(requireContext())

        // Inisialisasi inputan
        initializeViews()

        // Setup UI
        setupGenderDropdown()
        setupTextWatchers()
        setupClickListeners()
        setupAnimations()
        setupPhotoComponents()

        // Fetch data user
        fetchUserData()

        return binding.root
    }

    private fun initializeViews() {
        edNama = binding.edText1
        edNoHp = binding.edTex2
        edAlamat = binding.edTex4
        btnEdit = binding.buttonEdit
        btnCancel = binding.buttonCancel
        btnBack = binding.btnBack
        edJenisKelamin = binding.autoCompleteGender

        // Inisialisasi komponen foto
        imgProfile = binding.imgProfile
        btnChangePhoto = binding.btnChangePhoto
    }

    private fun setupPhotoComponents() {
        // Setup click listener untuk mengubah foto
        btnChangePhoto.setOnClickListener {
            checkPermissionAndShowImageDialog()
        }

        // Setup click listener untuk image profile juga
        imgProfile.setOnClickListener {
            checkPermissionAndShowImageDialog()
        }
    }

    private fun checkPermissionAndShowImageDialog() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                showImageSourceDialog()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Kamera", "Galeri")
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Pilih Sumber Gambar")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> openCamera()
                1 -> openGallery()
            }
        }
        builder.show()
    }

    private fun openCamera() {
        val photoFile = createImageFile()
        photoFile?.let {
            val photoURI = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                it
            )
            selectedImageUri = photoURI
            takePictureLauncher.launch(photoURI)
        }
    }

    private fun openGallery() {
        pickImageLauncher.launch("image/*")
    }

    private fun createImageFile(): File? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir = requireContext().getExternalFilesDir(null)
            val imageFile = File.createTempFile(
                "JPEG_${timeStamp}_",
                ".jpg",
                storageDir
            )
            currentPhotoPath = imageFile.absolutePath
            imageFile
        } catch (ex: IOException) {
            Toast.makeText(requireContext(), "Error creating image file", Toast.LENGTH_SHORT).show()
            null
        }
    }

    private fun loadImageToImageView(uri: Uri) {
        Glide.with(this)
            .load(uri)
            .apply(RequestOptions.circleCropTransform())
            .into(imgProfile)
    }

    private fun setupGenderDropdown() {
        val genderOptions = listOf("laki-laki", "perempuan")
        val genderAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, genderOptions)
        edJenisKelamin.setAdapter(genderAdapter)

        edJenisKelamin.setOnClickListener {
            edJenisKelamin.showDropDown()
        }
    }

    private fun setupTextWatchers() {
        // Text watcher untuk nama
        edNama.doOnTextChanged { text, _, _, _ ->
            checkDataChanged()
        }

        // Text watcher untuk no hp
        edNoHp.doOnTextChanged { text, _, _, _ ->
            checkDataChanged()
            validatePhoneNumber(text.toString())
        }

        // Text watcher untuk jenis kelamin
        edJenisKelamin.doOnTextChanged { text, _, _, _ ->
            checkDataChanged()
        }

        // Text watcher untuk alamat
        edAlamat.doOnTextChanged { text, _, _, _ ->
            checkDataChanged()
        }
    }

    private fun setupClickListeners() {
        // Existing logic
        btnEdit.setOnClickListener {
            if (validateInputs()) {
                updateData()
            }
        }

        // Enhanced cancel button logic
        btnCancel.setOnClickListener {
            if (isDataChanged) {
                showDiscardChangesDialog()
            } else {
                resetToOriginalData()
            }
        }

        // Back button logic
        btnBack.setOnClickListener {
            if (isDataChanged) {
                showDiscardChangesDialog()
            } else {
                requireActivity().onBackPressed()
            }
        }

        // Existing logout logic
        binding.btnLogout.setOnClickListener {
            if (isDataChanged) {
                showLogoutWithUnsavedChangesDialog()
            } else {
                performLogout()
            }
        }
    }

    private fun setupAnimations() {
        // Animasi untuk profile card
        val profileCard = binding.profileInfoCard
        val formCard = binding.settingsFormCard

        // Fade in animation
        profileCard.alpha = 0f
        formCard.alpha = 0f

        val profileAnimator = ObjectAnimator.ofFloat(profileCard, "alpha", 0f, 1f)
        val formAnimator = ObjectAnimator.ofFloat(formCard, "alpha", 0f, 1f)

        val animatorSet = AnimatorSet()
        animatorSet.playSequentially(profileAnimator, formAnimator)
        animatorSet.duration = 600
        animatorSet.interpolator = AccelerateDecelerateInterpolator()
        animatorSet.start()
    }

    private fun checkDataChanged() {
        val isPhotoChanged = selectedImageUri != null

        isDataChanged = (edNama.text.toString() != originalNama ||
                edNoHp.text.toString() != originalNoHp ||
                edJenisKelamin.text.toString() != originalJenisKelamin ||
                edAlamat.text.toString() != originalAlamat ||
                isPhotoChanged)

        updateButtonStates()
    }

    private fun updateButtonStates() {
        btnEdit.isEnabled = isDataChanged && validateInputs()
        btnCancel.isEnabled = isDataChanged

        // Visual feedback
        btnEdit.alpha = if (btnEdit.isEnabled) 1.0f else 0.5f
        btnCancel.alpha = if (btnCancel.isEnabled) 1.0f else 0.5f
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        // Validasi nama
        if (edNama.text.toString().trim().isEmpty()) {
            edNama.error = "Nama tidak boleh kosong"
            isValid = false
        } else if (edNama.text.toString().trim().length < 2) {
            edNama.error = "Nama minimal 2 karakter"
            isValid = false
        } else {
            edNama.error = null
        }

        // Validasi no hp
        if (edNoHp.text.toString().trim().isEmpty()) {
            edNoHp.error = "Nomor telepon tidak boleh kosong"
            isValid = false
        } else if (!isValidPhoneNumber(edNoHp.text.toString())) {
            edNoHp.error = "Format nomor telepon tidak valid"
            isValid = false
        } else {
            edNoHp.error = null
        }

        // Validasi jenis kelamin
        if (edJenisKelamin.text.toString().trim().isEmpty()) {
            edJenisKelamin.error = "Jenis kelamin harus dipilih"
            isValid = false
        } else {
            edJenisKelamin.error = null
        }

        // Validasi alamat
        if (edAlamat.text.toString().trim().isEmpty()) {
            edAlamat.error = "Alamat tidak boleh kosong"
            isValid = false
        } else if (edAlamat.text.toString().trim().length < 10) {
            edAlamat.error = "Alamat minimal 10 karakter"
            isValid = false
        } else {
            edAlamat.error = null
        }

        return isValid
    }

    private fun validatePhoneNumber(phone: String) {
        if (phone.isNotEmpty() && !isValidPhoneNumber(phone)) {
            edNoHp.error = "Format nomor telepon tidak valid"
        } else {
            edNoHp.error = null
        }
    }

    private fun isValidPhoneNumber(phone: String): Boolean {
        val phonePattern = "^(\\+62|62|0)[0-9]{9,13}$"
        return phone.matches(phonePattern.toRegex())
    }

    private fun showDiscardChangesDialog() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Buang Perubahan?")
        builder.setMessage("Anda memiliki perubahan yang belum disimpan. Apakah Anda yakin ingin membuang perubahan?")
        builder.setPositiveButton("Buang") { _, _ ->
            resetToOriginalData()
        }
        builder.setNegativeButton("Batal") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun showLogoutWithUnsavedChangesDialog() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Logout dengan Perubahan Belum Disimpan?")
        builder.setMessage("Anda memiliki perubahan yang belum disimpan. Apakah Anda yakin ingin logout?")
        builder.setPositiveButton("Logout") { _, _ ->
            performLogout()
        }
        builder.setNegativeButton("Batal") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun performLogout() {
        sessionManager.logout()
        val intent = Intent(requireContext(), Login::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun resetToOriginalData() {
        edNama.setText(originalNama)
        edNoHp.setText(originalNoHp)
        edJenisKelamin.setText(originalJenisKelamin)
        edAlamat.setText(originalAlamat)

        // Reset foto ke original
        selectedImageUri = null
        if (originalFoto.isNotEmpty()) {
            loadImageFromUrl(originalFoto)
        } else {
            imgProfile.setImageResource(R.drawable.ic_person)
        }

        isDataChanged = false
        updateButtonStates()
    }

    private fun showLoadingState(isLoading: Boolean) {
        btnEdit.isEnabled = !isLoading
        btnCancel.isEnabled = !isLoading && isDataChanged
        btnEdit.text = if (isLoading) "Memperbarui..." else "Perbarui Profil"
        btnEdit.alpha = if (isLoading) 0.5f else 1.0f
    }

    private fun prepareImagePart(): MultipartBody.Part? {
        selectedImageUri?.let { uri ->
            return try {
                val inputStream = requireContext().contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)

                // Compress bitmap
                val file = File(requireContext().cacheDir, "profile_${System.currentTimeMillis()}.jpg")
                val outputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                outputStream.flush()
                outputStream.close()

                val requestFile = RequestBody.create("image/jpeg".toMediaTypeOrNull(), file)
                MultipartBody.Part.createFormData("foto", file.name, requestFile)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
        return null
    }

    // Enhanced updateData method with photo support
    private fun updateData() {
        val nama = edNama.text.toString().trim()
        val noHp = edNoHp.text.toString().trim()
        val jk = edJenisKelamin.text.toString().trim()
        val alamat = edAlamat.text.toString().trim()

        if (nama.isBlank() || noHp.isBlank() || jk.isBlank() || alamat.isBlank()) {
            Toast.makeText(context, "Harap isi semua data!", Toast.LENGTH_SHORT).show()
            return
        }

        showLoadingState(true)
        val token = "Bearer ${sessionManager.getToken()}"

        val api = NetworkConfig.getServices()

        // Check if photo is selected
        val photoPart = prepareImagePart()

        if (photoPart != null) {
            // Update with photo - use multipart
            val namaBody = RequestBody.create("text/plain".toMediaTypeOrNull(), nama)
            val noHpBody = RequestBody.create("text/plain".toMediaTypeOrNull(), noHp)
            val jkBody = RequestBody.create("text/plain".toMediaTypeOrNull(), jk)
            val alamatBody = RequestBody.create("text/plain".toMediaTypeOrNull(), alamat)

            api.updatePenjagaWithPhoto(token, namaBody, noHpBody, jkBody, alamatBody, photoPart)
                .enqueue(object : Callback<UpdateResponse> {
                    override fun onResponse(
                        call: Call<UpdateResponse>,
                        response: Response<UpdateResponse>
                    ) {
                        handleUpdateResponse(response, nama, noHp, jk, alamat)
                    }

                    override fun onFailure(call: Call<UpdateResponse>, t: Throwable) {
                        handleUpdateFailure(t)
                    }
                })
        } else {
            // Update without photo - use form data
            api.updateProfile(token, nama, noHp, jk, alamat)
                .enqueue(object : Callback<UpdateResponse> {
                    override fun onResponse(
                        call: Call<UpdateResponse>,
                        response: Response<UpdateResponse>
                    ) {
                        handleUpdateResponse(response, nama, noHp, jk, alamat)
                    }

                    override fun onFailure(call: Call<UpdateResponse>, t: Throwable) {
                        handleUpdateFailure(t)
                    }
                })
        }
    }

    private fun handleUpdateResponse(
        response: Response<UpdateResponse>,
        nama: String,
        noHp: String,
        jk: String,
        alamat: String
    ) {
        showLoadingState(false)
        if (response.isSuccessful) {
            Toast.makeText(context, "Data berhasil diupdate", Toast.LENGTH_SHORT).show()
            // Update original data setelah berhasil update
            originalNama = nama
            originalNoHp = noHp
            originalJenisKelamin = jk
            originalAlamat = alamat

            // Reset selected image since it's now saved
            selectedImageUri = null

            isDataChanged = false
            updateButtonStates()

            // Update display name
            binding.txtNamaSetting.text = nama

            // Refresh user data to get updated photo URL
            fetchUserData()
        } else {
            val errorBody = response.errorBody()?.string()
            Toast.makeText(context, "Gagal mengupdate data: $errorBody", Toast.LENGTH_LONG).show()
        }
    }

    private fun handleUpdateFailure(t: Throwable) {
        showLoadingState(false)
        Toast.makeText(context, "Error: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
    }

    // Enhanced fetchUserData method
    private fun fetchUserData() {
        val token = "Bearer ${session.getToken()}"
        apiService.getUserr(token).enqueue(object : Callback<UserRespon> {
            override fun onResponse(call: Call<UserRespon>, response: Response<UserRespon>) {
                if (response.isSuccessful) {
                    val user = response.body()?.data
                    if (user != null) {
                        // Update UI dengan data user
                        val nama = user.user?.nama ?: "Nama Tidak Tersedia"
                        val email = user.user?.email ?: "Email tidak tersedia"
                        val noHp = user.no_hp ?: ""
                        val jk = user.jenis_kelamin ?: ""
                        val alamat = user.alamat ?: ""
                        val fotoUrl = user.foto_url ?: ""

                        // Update original data
                        originalNama = nama
                        originalNoHp = noHp
                        originalJenisKelamin = jk
                        originalAlamat = alamat
                        originalFoto = fotoUrl

                        // Update UI
                        binding.txtNamaSetting.text = nama
                        binding.txtEmailSetting.text = email
                        edNama.setText(nama)
                        edNoHp.setText(noHp)
                        edJenisKelamin.setText(jk)
                        edAlamat.setText(alamat)

                        // Load profile photo
                        if (fotoUrl.isNotEmpty()) {
                            loadImageFromUrl(fotoUrl)
                        } else {
                            imgProfile.setImageResource(R.drawable.ic_person)
                        }

                        // Reset data changed flag
                        isDataChanged = false
                        updateButtonStates()
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

    private fun loadImageFromUrl(url: String) {
        if (url.isNotEmpty()) {
            Glide.with(this)
                .load(url)
                .apply(RequestOptions.circleCropTransform())
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .into(imgProfile)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clean up jika diperlukan
    }
}