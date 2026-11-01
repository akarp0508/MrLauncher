package com.akarp0508.mrlauncher.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale
import kotlin.math.roundToInt

data class Weather(
    val temperature: Int,
    /** WMO weather interpretation code. */
    val code: Int,
    val isDay: Boolean = true,
)

/** Open-Meteo: free, no API key, no account. */
class WeatherRepository {

    suspend fun searchLocations(query: String): List<WeatherLocation> = withContext(Dispatchers.IO) {
        val name = URLEncoder.encode(query.trim(), "UTF-8")
        val language = Locale.getDefault().language
        val json = getJson("$GEOCODING_URL?name=$name&count=8&language=$language&format=json")
        val results = json.optJSONArray("results") ?: return@withContext emptyList()
        List(results.length()) { i ->
            val result = results.getJSONObject(i)
            val label = listOf("name", "admin1", "country")
                .map { result.optString(it) }
                .filter { it.isNotEmpty() }
                .distinct()
                .joinToString(", ")
            WeatherLocation(label, result.getDouble("latitude"), result.getDouble("longitude"))
        }
    }

    suspend fun currentWeather(location: WeatherLocation, unit: TemperatureUnit): Weather =
        withContext(Dispatchers.IO) {
            val unitParam = when (unit) {
                TemperatureUnit.Celsius -> "celsius"
                TemperatureUnit.Fahrenheit -> "fahrenheit"
            }
            val json = getJson(
                "$FORECAST_URL?latitude=${location.latitude}&longitude=${location.longitude}" +
                    "&current=temperature_2m,weather_code,is_day&temperature_unit=$unitParam",
            )
            val current = json.getJSONObject("current")
            Weather(
                temperature = current.getDouble("temperature_2m").roundToInt(),
                code = current.getInt("weather_code"),
                isDay = current.optInt("is_day", 1) == 1,
            )
        }

    private fun getJson(url: String): JSONObject {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = TIMEOUT_MS
        connection.readTimeout = TIMEOUT_MS
        try {
            if (connection.responseCode !in 200..299) throw IOException("HTTP ${connection.responseCode}")
            return JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        const val GEOCODING_URL = "https://geocoding-api.open-meteo.com/v1/search"
        const val FORECAST_URL = "https://api.open-meteo.com/v1/forecast"
        const val TIMEOUT_MS = 10_000
    }
}
