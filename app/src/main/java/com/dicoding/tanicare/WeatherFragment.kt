package com.dicoding.tanicare

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.tanicare.databinding.FragmentWeatherBinding
import com.dicoding.tanicare.helper.HourlyForecastAdapter
import com.dicoding.tanicare.helper.SharedPreferencesManager
import com.dicoding.tanicare.helper.ThreeDayForecastAdapter
import com.dicoding.tanicare.helper.weather.WeatherClient
import com.dicoding.tanicare.helper.weather.WeatherData
import com.dicoding.tanicare.helper.weather.WeatherResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WeatherFragment : Fragment() {
    private var _binding: FragmentWeatherBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferencesManager: SharedPreferencesManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        sharedPreferencesManager = SharedPreferencesManager(requireContext())
        _binding = FragmentWeatherBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        fetchWeatherForecast(sharedPreferencesManager.getZoneCode()) // Ganti dengan kode wilayah
    }

    private fun setupRecyclerViews() {
        binding.recyclerHourlyForecast.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerThreeDayForecast.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
    }

    private fun fetchWeatherForecast(regionCode: String) {
        WeatherClient.instance.getWeatherForecast(regionCode).enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    val data = response.body()?.forecast ?: emptyList()

                    // Update bagian atas: UV Index dan Humidity
                    val currentWeather = data.firstOrNull()
                    if (currentWeather != null) {
                        val uvIndex = translateCloudCoverageToUVIndex(currentWeather.tcc)
                        binding.tvUvIndex.text = "UV Index: $uvIndex"
                        binding.tvHumidity.text = "Humidity: ${currentWeather.hu}%"
                    }

                    // Data untuk Hourly Forecast
                    val hourlyData = data.take(5).map {
                        // Mengonversi menjadi WeatherData dengan data yang relevan
                        WeatherData(
                            datetime = it.datetime,
                            t = it.t,
                            hu = it.hu,
                            tcc = it.tcc,
                            weather_desc = it.weather_desc,
                            local_datetime = it.local_datetime,
                            image = it.image // Bisa disesuaikan jika perlu
                        )
                    }

                    // Data untuk 3-Day Forecast
                    val threeDayData = data.groupBy { it.local_datetime.substring(0, 10) }
                        .map { entry ->
                            val firstData = entry.value.first()
                            WeatherData(
                                datetime = firstData.datetime,
                                t = firstData.t,
                                hu = firstData.hu,
                                tcc = firstData.tcc,
                                weather_desc = firstData.weather_desc,
                                local_datetime = firstData.local_datetime,
                                image = firstData.image
                            )
                        }

                    // Set adapter
                    binding.recyclerHourlyForecast.adapter = HourlyForecastAdapter(hourlyData)
                    binding.recyclerThreeDayForecast.adapter = ThreeDayForecastAdapter(threeDayData)
                } else {
                    Toast.makeText(requireContext(), "Gagal mengambil data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Kesalahan jaringan: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Fungsi konversi cloudCoverage ke UV Index
    private fun translateCloudCoverageToUVIndex(cloudCoverage: Int): Int {
        if (cloudCoverage < 0 || cloudCoverage > 100) {
            throw IllegalArgumentException("Persentase tutupan awan harus antara 0 dan 100")
        }

        return when {
            cloudCoverage in 0..10 -> 8  // Minimal tutupan awan, UV Index tertinggi
            cloudCoverage in 11..30 -> 7  // Sedikit tutupan awan
            cloudCoverage in 31..50 -> 5  // Setengah tutupan awan
            cloudCoverage in 51..70 -> 3  // Banyak tutupan awan
            cloudCoverage in 71..90 -> 2  // Hampir seluruhnya tertutup awan
            else -> 1  // Tutupan awan hampir 100%, UV Index rendah
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

