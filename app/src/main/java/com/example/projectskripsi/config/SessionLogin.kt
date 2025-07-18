package com.example.projectskripsi.config

import android.content.Context
import com.example.projectskripsi.model.UserResponse

class SessionLogin(context: Context) {
    private val prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    private val editor = prefs.edit()

    fun saveToken(token: String) {
        editor.putString("user_token", token).apply()
    }


    fun getToken(): String? {
        return prefs.getString("user_token", null)
    }

    fun saveEmail(email: String) {
        editor.putString("user_email", email).apply()
    }

    fun getEmail(): String? {
        return prefs.getString("user_email", null)
    }

    fun isLoggedIn(): Boolean {
        return getToken() != null
    }

    fun logout() {
        editor.clear().apply()
    }

    fun setUser(user: UserResponse) {
        val editor = prefs.edit()
        editor.putString("name", user.nama)
        editor.putString("email", user.email)
        editor.putString("role", user.role)
        editor.putString("token", user.token)
        editor.apply()
    }


    fun getRole(): String? = prefs.getString("role", null)


    fun getId(): Int? {
        val id = prefs.getInt("user_id", -1)
        return if (id == -1) null else id
    }
    fun saveUserId(id: Int) {
        prefs.edit().putInt("user_id", id).apply()
    }

    fun getUserId(): Int {
        return prefs.getInt("user_id", 0)
    }


}