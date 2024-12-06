package com.dicoding.tanicare.helper

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "AppPreferences"
        private const val DARK_MODE_KEY = "dark_mode"
        private const val USER_ID = "user_id"
        private const val AUTH_TOKEN_KEY = "auth_token" // Key untuk token
        private const val ZONE_CODE = "zone_code" // kode wilayah
        private const val ZONE_NAME = "zone_name"
        private const val USERNAME = "username"
        private const val ABOUT = "about"
        private const val IMAGE_URL = "image_url"
    }

    // Fungsi untuk memeriksa apakah dark mode diaktifkan
    fun isDarkModeEnabled(): Boolean {
        return sharedPreferences.getBoolean(DARK_MODE_KEY, false)
    }

    // Fungsi untuk mengatur status dark mode
    fun setDarkModeEnabled(enabled: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(DARK_MODE_KEY, enabled)
        editor.apply()
    }

    // Fungsi untuk menyimpan token login (auth token)
    fun saveAuthToken(token: String) {
        val editor = sharedPreferences.edit()
        editor.putString(AUTH_TOKEN_KEY, token)
        editor.apply()
    }

    fun saveUserId(token: String) {
        val editor = sharedPreferences.edit()
        editor.putString(USER_ID, token)
        editor.apply()
    }

    fun saveZoneCode(zone: String) {
        val editor = sharedPreferences.edit()
        editor.putString(ZONE_CODE, zone)
        editor.apply()
    }

    fun saveZoneName(zone: String) {
        val editor = sharedPreferences.edit()
        editor.putString(ZONE_NAME, zone)
        editor.apply()
    }

    fun saveUserInfo(zonename: String, username: String, about: String, image: String) {
        val editor = sharedPreferences.edit()
        editor.putString(ZONE_NAME, zonename)
        editor.putString(USERNAME, username)
        editor.putString(ABOUT, about)
        editor.putString(IMAGE_URL, image)
        editor.apply()
    }

    // Fungsi untuk mengambil token login (auth token)
    fun getAuthToken(): String? {
        return sharedPreferences.getString(AUTH_TOKEN_KEY, null)
    }

    fun getUserId(): String? {
        return sharedPreferences.getString(USER_ID, "V0qacwIL1tMFncTfYP1mYbb3wh83")
    }

    fun getZoneCode(): String {
        return sharedPreferences.getString(ZONE_CODE, "35.01.04.1013").toString()
    }

    fun getZoneName(): String {
        return sharedPreferences.getString(ZONE_NAME, "Rungkut").toString()
    }

    fun getUsername(): String {
        return sharedPreferences.getString(USERNAME, "Username").toString()
    }

    fun getAbout(): String {
        return sharedPreferences.getString(ABOUT, "Lorem Ipsum").toString()
    }

    fun getImageUrl(): String {
        return sharedPreferences.getString("image_url", "") ?: ""
    }

    // Fungsi untuk menghapus token login
    fun clearAuthToken() {
        val editor = sharedPreferences.edit()
        editor.remove(AUTH_TOKEN_KEY)
        editor.apply()
    }

    fun clearUserInfo() {
        val editor = sharedPreferences.edit()
        editor.remove(ZONE_NAME)
        editor.remove(USERNAME)
        editor.remove(ABOUT)
        editor.remove(USER_ID)
        editor.remove(AUTH_TOKEN_KEY)
        editor.apply()
    }

    fun clearUserId() {
        val editor = sharedPreferences.edit()
        editor.remove(USER_ID)
        editor.apply()
    }


}
