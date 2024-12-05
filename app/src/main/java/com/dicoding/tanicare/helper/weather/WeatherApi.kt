package com.dicoding.tanicare.helper.weather

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("prakiraan-cuaca")
    fun getWeather(@Query("adm4") adm4: String): Call<ApiResponse>
}