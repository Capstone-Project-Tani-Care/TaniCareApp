package com.dicoding.tanicare.helper.weather

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherViewModel : ViewModel() {

    private val _weatherLiveData = MutableLiveData<WeatherUIModel>()
    val weatherLiveData: LiveData<WeatherUIModel> get() = _weatherLiveData

    fun fetchWeatherData(adm4: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.bmkg.go.id/publik/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(WeatherApiService::class.java)
        service.getWeather(adm4).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    val cuacaList = response.body()?.data?.get(0)?.cuaca?.flatten() ?: emptyList()

                    // Extract first weather data as summary
                    val firstWeatherData = cuacaList.firstOrNull() ?: return
                    val tcc = firstWeatherData.tcc
                    val uvIndex = when {
                        tcc >= 80 -> "Low"
                        tcc >= 50 -> "Moderate"
                        else -> "High"
                    }

                    _weatherLiveData.postValue(
                        WeatherUIModel(
                            city = response.body()?.lokasi?.kecamatan ?: "",
                            temperature = "${firstWeatherData.t}°",
                            condition = firstWeatherData.weather_desc,
                            humidity = "${firstWeatherData.hu}%",
                            uvIndex = uvIndex,
                            hourlyForecast = cuacaList // Pass all hourly data
                        )
                    )
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                // Handle failure
            }
        })
    }
}


data class WeatherUIModel(
    val city: String,
    val temperature: String,
    val condition: String,
    val humidity: String,
    val uvIndex: String,
    val hourlyForecast: List<Cuaca>
)
