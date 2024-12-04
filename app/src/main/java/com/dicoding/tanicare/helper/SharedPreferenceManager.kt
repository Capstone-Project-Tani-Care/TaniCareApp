package com.dicoding.tanicare.helper

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "AppPreferences"
        private const val DARK_MODE_KEY = "dark_mode"
        private const val AUTH_TOKEN_KEY = "auth_token" // Key untuk token
        private const val ZONE_CODE = "zone_code" // kode wilayah
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

    fun saveZoneCode(zone: String) {
        val editor = sharedPreferences.edit()
        editor.putString(ZONE_CODE, zone)
        editor.apply()
    }

    // Fungsi untuk mengambil token login (auth token)
    fun getAuthToken(): String? {
        return sharedPreferences.getString(AUTH_TOKEN_KEY, null) // Mengembalikan null jika token tidak ada
    }

    fun getZoneCode(): String {
        return sharedPreferences.getString(ZONE_CODE, "35.01.04.1013").toString()
    }

    // Fungsi untuk menghapus token login
    fun clearAuthToken() {
        val editor = sharedPreferences.edit()
        editor.remove(AUTH_TOKEN_KEY)
        editor.apply()
    }
}
