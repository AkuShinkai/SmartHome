package com.example.smarthome.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarthome.api.WeatherRepository
import com.example.smarthome.data.AirQualityResponse
import com.example.smarthome.data.ForecastResponse
import com.example.smarthome.data.WeatherResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WeatherViewModel(private val repository: WeatherRepository) : ViewModel() {
    private val _weather = MutableStateFlow<WeatherResponse?>(null)
    val weather: StateFlow<WeatherResponse?> = _weather

    private val _forecast = MutableStateFlow<ForecastResponse?>(null)
    val forecast: StateFlow<ForecastResponse?> = _forecast

    private val _airQuality = MutableStateFlow<AirQualityResponse?>(null)
    val airQuality: StateFlow<AirQualityResponse?> = _airQuality

    fun fetchWeather(city: String, apiKey: String) {
        viewModelScope.launch {
            _weather.value = repository.getWeather(city, apiKey)
            _forecast.value = repository.getForecast(city, apiKey)
            _airQuality.value = repository.getAirQuality(
                _weather.value?.coord?.lat ?: 0.0,
                _weather.value?.coord?.lon ?: 0.0,
                apiKey
            )
        }
    }
}
