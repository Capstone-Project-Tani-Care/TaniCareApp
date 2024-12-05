package com.dicoding.tanicare.helper.weather

data class ApiResponse(
    val lokasi: Lokasi,
    val data: List<WeatherData>
)

data class Lokasi(
    val provinsi: String,
    val kotkab: String,
    val kecamatan: String,
    val desa: String
)

data class WeatherData(
    val lokasi: Lokasi,
    val cuaca: List<List<Cuaca>>
)

data class Cuaca(
    val datetime: String,
    val t: Int,
    val tcc: Int,
    val tp: Double,
    val weather_desc: String,
    val hu: Int,
    val image: String,
    val local_datetime: String
)

