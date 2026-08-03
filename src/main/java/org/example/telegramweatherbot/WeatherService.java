package org.example.telegramweatherbot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class WeatherService {

    @Value("${weather.api.key}")
    private String API_KEY;

    private final String BASE_URL = "http://api.weatherapi.com/v1/";
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    public String getCurrentWeather(String city) {
        try {
            String url = BASE_URL + "current.json?key=" + API_KEY + "&q=" + city + "&lang=ru";
            String response = restTemplate.getForObject(url, String.class);
            JsonNode json = mapper.readTree(response);

            String cityName = json.get("location").get("name").asText();
            String region = json.get("location").get("region").asText();
            String country = json.get("location").get("country").asText();
            String localTime = json.get("location").get("localtime").asText();

            double temp = json.get("current").get("temp_c").asDouble();
            double feelsLike = json.get("current").get("feelslike_c").asDouble();
            String condition = json.get("current").get("condition").get("text").asText();
            int humidity = json.get("current").get("humidity").asInt();
            double windKph = json.get("current").get("wind_kph").asDouble();
            String windDir = json.get("current").get("wind_dir").asText();
            int cloud = json.get("current").get("cloud").asInt();
            double uvIndex = json.get("current").get("uv").asDouble();
            double precipMm = json.get("current").get("precip_mm").asDouble();

            String emoji = getWeatherEmoji(condition);

            String rainInfo = "";
            if (precipMm > 0) {
                rainInfo = String.format("🌧️ Осадки: %.1f мм\n", precipMm);
            }

            return String.format(
                    "🌤 Погода в городе %s\n" +
                            "📍 %s, %s\n" +
                            "🕐 %s\n\n" +
                            "%s Температура: %.1f°C (ощущается как %.1f°C)\n" +
                            "📝 Описание: %s\n" +
                            "💧 Влажность: %d%%\n" +
                            "💨 Ветер: %.1f км/ч, %s\n" +
                            "☁️ Облачность: %d%%\n" +
                            "☀️ УФ-индекс: %.1f\n" +
                            "%s",
                    cityName, region, country, localTime,
                    emoji, temp, feelsLike, condition, humidity,
                    windKph, windDir, cloud, uvIndex, rainInfo);

        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Город не найден!\n\n" +
                    "Проверьте название города (на английском).\n" +
                    "Пример: Moscow, London, Kazan, Russia";
        }
    }

    public String getWeeklyForecast(String city) {
        try {
            String url = BASE_URL + "forecast.json?key=" + API_KEY + "&q=" + city + "&days=7&lang=ru";
            String response = restTemplate.getForObject(url, String.class);
            JsonNode json = mapper.readTree(response);

            String cityName = json.get("location").get("name").asText();
            var forecastDays = json.get("forecast").get("forecastday");

            StringBuilder result = new StringBuilder();
            result.append(String.format("📅 Прогноз на неделю для %s:%n%n", cityName));

            for (var day : forecastDays) {
                String date = day.get("date").asText();
                double maxTemp = day.get("day").get("maxtemp_c").asDouble();
                double minTemp = day.get("day").get("mintemp_c").asDouble();
                double avgTemp = day.get("day").get("avgtemp_c").asDouble();
                String condition = day.get("day").get("condition").get("text").asText();
                int chanceOfRain = day.get("day").get("daily_chance_of_rain").asInt();
                String emoji = getWeatherEmoji(condition);

                String rainEmoji = "";
                if (chanceOfRain > 30) {
                    rainEmoji = " 🌧️";
                }

                String formattedDate = formatDate(date);

                result.append(String.format("• %s: %.1f°C / %.1f°C (сред: %.1f°C) %s %s%s%n",
                        formattedDate, maxTemp, minTemp, avgTemp, emoji, condition, rainEmoji));
            }

            return result.toString();

        } catch (Exception e) {
            return "❌ Не удалось получить прогноз для города " + city + "\n" +
                    "Попробуйте позже или проверьте название города.";
        }
    }

    private String formatDate(String date) {
        try {
            String[] parts = date.split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int day = Integer.parseInt(parts[2]);

            String[] months = {"Янв", "Фев", "Мар", "Апр", "Май", "Июн",
                    "Июл", "Авг", "Сен", "Окт", "Ноя", "Дек"};
            return String.format("%d %s", day, months[month - 1]);
        } catch (Exception e) {
            return date;
        }
    }

    private String getWeatherEmoji(String condition) {
        condition = condition.toLowerCase();
        if (condition.contains("солнечно") || condition.contains("ясно")) return "☀️";
        if (condition.contains("облачно") || condition.contains("пасмурно")) return "☁️";
        if (condition.contains("небольшой дождь")) return "🌦️";
        if (condition.contains("дождь") || condition.contains("ливень")) return "🌧️";
        if (condition.contains("снег") || condition.contains("метель")) return "❄️";
        if (condition.contains("туман") || condition.contains("дымка")) return "🌫️";
        if (condition.contains("гроза") || condition.contains("буря")) return "⛈️";
        if (condition.contains("переменная облачность")) return "⛅";
        if (condition.contains("ясно с прояснениями")) return "🌤️";
        return "🌡️";
    }
}