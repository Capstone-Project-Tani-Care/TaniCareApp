package com.dicoding.tanicare.helper.weather

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("publik/prakiraan-cuaca")
    fun getWeatherForecast(
        @Query("adm4") regionCode: String
    ): Call<WeatherResponse>
}