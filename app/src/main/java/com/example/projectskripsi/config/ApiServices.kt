package com.example.projectskripsi.config

import com.example.projectskripsi.model.GeneralResponse
import com.example.projectskripsi.model.LastPaymentResponse
import com.example.projectskripsi.model.LoginResponse
import com.example.projectskripsi.model.Parkir
import com.example.projectskripsi.model.ParkirRequest
import com.example.projectskripsi.model.ParkirResponse
import com.example.projectskripsi.model.PendapatanResponse
import com.example.projectskripsi.model.RegisterResponse
import com.example.projectskripsi.model.RiwayatResponse
import com.example.projectskripsi.model.RiwayatResponseBody
import com.example.projectskripsi.model.StatistikRespon
import com.example.projectskripsi.model.StatistikResponse
import com.example.projectskripsi.model.UpdateResponse
import com.example.projectskripsi.model.UserRequest
import com.example.projectskripsi.model.UserRespon
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

// Fixed ApiServices.kt interface
interface ApiServices {
    @POST("login")
    fun login(@Body user: UserRequest): Call<LoginResponse>

    @POST("register")
    fun register(@Body user: Regis): Call<RegisterResponse>

    @POST("parkir")
    fun tambahParkir(@Body parkirRequest: ParkirRequest): Call<ParkirResponse>

    @POST("parkir")
    fun simpanParkir(@Body parkirRequest: ParkirRequest): Call<Any>

    @GET("parkir/aktif")
    fun getParkirBelumBayar(): Call<List<Parkir>>



    // Update profile penjaga tanpa foto - POST method with proper headers
    @FormUrlEncoded
    @POST("penjaga/update")
    @Headers("Accept: application/json", "Content-Type: application/x-www-form-urlencoded")
    fun updateProfile(
        @Header("Authorization") token: String,
        @Field("nama") nama: String,
        @Field("no_hp") noHp: String,
        @Field("jenis_kelamin") jenisKelamin: String,
        @Field("alamat") alamat: String
    ): Call<UpdateResponse>
    // Update profile penjaga dengan foto - POST method dengan multipart
    @Multipart
    @POST("penjaga/update")
    @Headers("Accept: application/json")
    fun updatePenjagaWithPhoto(
        @Header("Authorization") token: String,
        @Part("nama") nama: RequestBody,
        @Part("no_hp") noHp: RequestBody,
        @Part("jenis_kelamin") jenisKelamin: RequestBody,
        @Part("alamat") alamat: RequestBody,
        @Part foto: MultipartBody.Part
    ): Call<UpdateResponse>

    @FormUrlEncoded
    @POST("pengguna/update")
    @Headers("Accept: application/json", "Content-Type: application/x-www-form-urlencoded")
    fun updateProfilePengguna(
        @Header("Authorization") token: String,
        @Field("nama") nama: String,
        @Field("no_hp") noHp: String,
        @Field("jenis_kelamin") jenisKelamin: String,
        @Field("alamat") alamat: String
    ): Call<UpdateResponse>

    @Multipart
    @POST("pengguna/update")
    @Headers("Accept: application/json")
    fun updatePenggunaWithPhoto(
        @Header("Authorization") token: String,
        @Part("nama") nama: RequestBody,
        @Part("no_hp") noHp: RequestBody,
        @Part("jenis_kelamin") jenisKelamin: RequestBody,
        @Part("alamat") alamat: RequestBody,
        @Part foto: MultipartBody.Part
    ): Call<UpdateResponse>

    @GET("pengguna/profile")
    @Headers("Accept: application/json")
    fun getUser(@Header("Authorization") token: String): Call<UserRespon>

    @GET("penjaga/profile") // Sesuaikan dengan route Laravel Anda
    fun getUserr(@Header("Authorization") token: String): Call<UserRespon>

    @GET("pengguna/statistik")
    fun getStatistik(
        @Header("Authorization") token: String
    ): Call<StatistikRespon>

    @GET("riwayat/last")
    @Headers("Accept: application/json")
    fun getLastActivity(@Header("Authorization") token: String): Call<RiwayatResponse>

    @GET("pengguna/payment-method")
    @Headers("Accept: application/json")
    fun getLastPaymentMethod(
        @Header("Authorization") token: String
    ): Call<LastPaymentResponse>

    @PUT("pengguna/payment-method")
    @FormUrlEncoded
    @Headers("Accept: application/json")
    fun updateLastPaymentMethod(
        @Header("Authorization") token: String,
        @Field("payment_method") paymentMethod: String
    ): Call<GeneralResponse>

    @POST("midtrans/generate-token")
    @Headers("Accept: application/json", "Content-Type: application/json")
    fun getSnapToken(
        @Header("Authorization") token: String,
        @Body requestBody: Map<String, @JvmSuppressWildcards Any>
    ): Call<LastPaymentResponse>

    @GET("penjaga/statistik")
    @Headers("Accept: application/json")
    fun getStatistikPenjaga(
        @Header("Authorization") token: String
    ): Call<StatistikResponse>

    @GET("pendapatan/hari-ini")
    @Headers("Accept: application/json")
    fun getPendapatanHariIni(
        @Header("Authorization") token: String
    ): Call<PendapatanResponse>

    @GET("get-status-pembayaran")
    @Headers("Accept: application/json")
    fun getStatusPembayaran(
        @Header("Authorization") token: String,
        @Query("order_id") orderId: String
    ): Call<LastPaymentResponse>

    @DELETE("user")
    @Headers("Accept: application/json")
    fun deleteUser(
        @Header("Authorization") token: String
    ): Call<UpdateResponse>

    @POST("riwayat/store")
    @Headers("Accept: application/json", "Content-Type: application/json")
    fun simpanRiwayat(@Body body: RiwayatResponseBody): Call<Any>

    @GET("riwayat/user/{id}")
    @Headers("Accept: application/json")
    fun getRiwayatByUserId(@Path("id") userId: Int): Call<List<RiwayatResponse>>
}