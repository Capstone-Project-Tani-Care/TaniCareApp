package com.dicoding.tanicare.helper
import retrofit2.Call
import retrofit2.http.GET

interface ApiService {
    @GET("photos") // Endpoint API untuk mengambil foto
    fun getPhotos(): Call<List<String>>
}