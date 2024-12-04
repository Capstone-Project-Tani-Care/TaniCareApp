package com.dicoding.tanicare.helper.weather

data class WeatherResponse(
    val forecast: List<WeatherData>
)

data class WeatherData(
    val datetime: String,         // UTC waktu
    val t: Int,                   // Suhu
    val hu: Int,               // Hujan
    val tcc: Int,               // Tutupan awan dalam persentase
    val weather_desc: String,     // Deskripsi cuaca
    val local_datetime: String,   // Waktu lokal
    val image: String             // URL gambar ikon cuaca
)