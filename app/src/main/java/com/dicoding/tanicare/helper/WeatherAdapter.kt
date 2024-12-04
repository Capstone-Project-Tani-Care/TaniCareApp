package com.dicoding.tanicare.helper

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dicoding.tanicare.R
import com.dicoding.tanicare.databinding.ItemHourlyForecastBinding
import com.dicoding.tanicare.databinding.ItemThreeDayForecastBinding
import com.dicoding.tanicare.helper.weather.WeatherData

data class HourlyForecast(
    val temperature: Int,
    val precipitation: Int,
    val iconResId: Int
)

data class ThreeDayForecast(
    val day: String,
    val tempRange: String,
    val iconResId: Int
)

class HourlyForecastAdapter(private val data: List<WeatherData>) :
    RecyclerView.Adapter<HourlyForecastAdapter.HourlyViewHolder>() {

    inner class HourlyViewHolder(private val binding: ItemHourlyForecastBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: WeatherData) {
            binding.tvHourTemp.text = "${item.t}°C"
            binding.tvHourCloudCoverage.text = "${item.tcc}%"
            Glide.with(binding.root.context)
                .load(item.image)
                .into(binding.imgHourWeather)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourlyViewHolder {
        val binding = ItemHourlyForecastBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HourlyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HourlyViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size
}

class ThreeDayForecastAdapter(private val data: List<WeatherData>) :
    RecyclerView.Adapter<ThreeDayForecastAdapter.ThreeDayViewHolder>() {

    inner class ThreeDayViewHolder(private val binding: ItemThreeDayForecastBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: WeatherData) {
            binding.tvDay.text = item.local_datetime.substring(0, 10) // Ambil tanggal
            binding.tvDayTempRange.text = "${item.t}°C"
            Glide.with(binding.root.context)
                .load(item.image)
                .into(binding.imgDayWeather)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThreeDayViewHolder {
        val binding = ItemThreeDayForecastBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ThreeDayViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ThreeDayViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size
}
