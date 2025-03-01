package com.example.smarthome.data

data class WeatherResponse(
    val coord: Coord,  // Tambahkan koordinat lokasi
    val main: Main,
    val weather: List<Weather>,
    val wind: Wind,
    val name: String
)

data class Coord(val lat: Double, val lon: Double) // Tambahkan ini untuk koordinat
data class Main(val temp: Double, val feels_like: Double, val humidity: Int)
data class Weather(val main: String)
data class Wind(val speed: Double)
