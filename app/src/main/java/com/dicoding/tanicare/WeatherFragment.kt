package com.dicoding.tanicare

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager

import com.dicoding.tanicare.databinding.FragmentWeatherBinding
import com.dicoding.tanicare.helper.HourlyForecastAdapter
import com.dicoding.tanicare.helper.SharedPreferencesManager

import com.dicoding.tanicare.helper.ThreeDayForecastAdapter
import com.dicoding.tanicare.helper.weather.WeatherViewModel



class WeatherFragment : Fragment(R.layout.fragment_weather) {
    private lateinit var binding: FragmentWeatherBinding
    private val viewModel: WeatherViewModel by viewModels()
    private lateinit var sharedPreferencesManager: SharedPreferencesManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentWeatherBinding.bind(view)
        sharedPreferencesManager = SharedPreferencesManager(requireContext())

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (findNavController().currentDestination?.id == R.id.WeatherFragment) {
                    findNavController().navigateUp()
                }
            }
        })

        // Initialize adapters
        val hourlyAdapter = HourlyForecastAdapter(emptyList())
        val threeDayAdapter = ThreeDayForecastAdapter(emptyList())

        // Set up RecyclerView for hourly forecast
        binding.recyclerHourlyForecast.adapter = hourlyAdapter
        binding.recyclerHourlyForecast.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)


        // Set up RecyclerView for 3-day forecast
        binding.recyclerThreeDayForecast.adapter = threeDayAdapter
        binding.recyclerThreeDayForecast.layoutManager =
            LinearLayoutManager(requireContext())

        // Observe data
        viewModel.weatherLiveData.observe(viewLifecycleOwner) { data ->
            binding.tvCity.text = data.city
            binding.tvTemperature.text = data.temperature
            binding.tvCondition.text = data.condition
            binding.tvHumidity.text = data.humidity
            binding.tvUvIndex.text = data.uvIndex
            hourlyAdapter.updateItems(data.hourlyForecast)
            threeDayAdapter.updateItems(data.hourlyForecast.chunked(8)) // Group into 8 intervals per day
        }

        viewModel.fetchWeatherData(sharedPreferencesManager.getZoneCode())
    }
}
