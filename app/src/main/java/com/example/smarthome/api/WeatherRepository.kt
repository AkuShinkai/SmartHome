package com.example.smarthome.api

import com.example.smarthome.data.AirQualityResponse
import com.example.smarthome.data.ForecastResponse
import com.example.smarthome.data.WeatherResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherRepository {
    private val api: WeatherApi

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        api = retrofit.create(WeatherApi::class.java)
    }

    suspend fun getWeather(city: String, apiKey: String): WeatherResponse {
        return api.getWeather(city, apiKey)
    }

    suspend fun getForecast(city: String, apiKey: String): ForecastResponse {
        return api.getForecast(city, apiKey)
    }

    suspend fun getAirQuality(lat: Double, lon: Double, apiKey: String): AirQualityResponse {
        return api.getAirQuality(lat, lon, apiKey)
    }
}
