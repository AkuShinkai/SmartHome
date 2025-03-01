package com.example.smarthome.data

data class ForecastResponse(
    val list: List<ForecastItem>
)

data class ForecastItem(
    val dt_txt: String, // Waktu prakiraan dalam format String
    val main: Main,
    val weather: List<Weather>,
    val wind: Wind
)
