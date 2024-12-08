package com.dicoding.tanicare.helper

import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.tanicare.R
import com.dicoding.tanicare.helper.weather.Cuaca
import java.util.Date
import java.util.Locale

class HourlyForecastAdapter(private var items: List<Cuaca>) :
    RecyclerView.Adapter<HourlyForecastAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvHourTemp: TextView = itemView.findViewById(R.id.tv_hour_temp)
        val tvHourCloudCoverage: TextView = itemView.findViewById(R.id.tv_hour_cloud_coverage)
        val imgHourWeather: ImageView = itemView.findViewById(R.id.img_hour_weather)
        val tvHour: TextView = itemView.findViewById(R.id.tv_hour)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hourly_forecast, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvHourTemp.text = "${item.t}°"
        holder.tvHourCloudCoverage.text = "${item.tcc}%"

        val weatherIconResId = getWeatherIcon(item.weather_desc)
        holder.imgHourWeather.setImageResource(weatherIconResId)

        // Format and display the hour
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("h a", Locale.getDefault()) // "h a" -> 7 PM
        try {
            val date = inputFormat.parse(item.datetime)
            holder.tvHour.text = outputFormat.format(date)
        } catch (e: Exception) {
            holder.tvHour.text = "-" // Fallback in case of error
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<Cuaca>) {
        items = newItems
        notifyDataSetChanged()
    }
}

class ThreeDayForecastAdapter(private var items: List<List<Cuaca>>) :
    RecyclerView.Adapter<ThreeDayForecastAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDay: TextView = itemView.findViewById(R.id.tv_day)
        val tvDayTempRange: TextView = itemView.findViewById(R.id.tv_day_temp_range)
        val imgDayWeather: ImageView = itemView.findViewById(R.id.img_day_weather)
        val tvTcc: TextView = itemView.findViewById(R.id.tv_tcc)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_three_day_forecast, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dayForecast = items[position]

        val firstItem = dayForecast.firstOrNull() ?: return

        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        try {
            val date = inputFormat.parse(firstItem.datetime)
            holder.tvDay.text = getDayLabel(date)
        } catch (e: Exception) {
            holder.tvDay.text = "-" // Fallback in case of error
        }

        val minTemp = dayForecast.minOfOrNull { it.t } ?: 0
        val maxTemp = dayForecast.maxOfOrNull { it.t } ?: 0
        holder.tvDayTempRange.text = "$minTemp° - $maxTemp°"

        // Load weather icon
        val weatherIconResId = getWeatherIcon(firstItem.weather_desc)
        holder.imgDayWeather.setImageResource(weatherIconResId)

        val avgTcc = dayForecast.map { it.tcc }.average().toInt() // Rata-rata TCC dalam persen
        holder.tvTcc.text = "$avgTcc%"
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<List<Cuaca>>) {
        items = newItems
        notifyDataSetChanged()
    }

    private fun getDayLabel(date: Date?): String {
        if (date == null) return "-"
        val calendar = Calendar.getInstance()
        val today = Calendar.getInstance()

        // Compare the date with today
        calendar.time = date
        return when {
            calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) -> "Today"

            calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) + 1 -> SimpleDateFormat("EEEE", Locale.getDefault()).format(date)

            else -> SimpleDateFormat("EEEE", Locale.getDefault()).format(date) // "Monday", "Tuesday", etc.
        }
    }

}


private fun getWeatherIcon(weatherDesc: String): Int {
    return when (weatherDesc) {
        "Cerah" -> R.drawable.ic_sunny
        "Cerah Berawan" -> R.drawable.ic_cloudy //R.drawable.ic_partly_cloudy
        "Berawan" -> R.drawable.ic_cloudy
        "Hujan Ringan", "Hujan Lebat" -> R.drawable.ic_rain
        "Petir", "Hujan Petir" -> R.drawable.ic_rain//R.drawable.ic_thunderstorm
        "Udara Kabur" -> R.drawable.ic_cloudy
        else -> R.drawable.ic_sunny // Fallback jika tidak ada kecocokan
    }
}