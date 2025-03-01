package com.example.smarthome.data

data class AirQualityResponse(
    val list: List<AirQualityItem>
)

data class AirQualityItem(
    val main: AirQualityIndex,
    val components: AirQualityComponents
)

data class AirQualityIndex(val aqi: Int) // AQI dari 1-5

data class AirQualityComponents(
    val pm2_5: Double,
    val pm10: Double,
    val co: Double,
    val no2: Double,
    val so2: Double,
    val o3: Double
)
