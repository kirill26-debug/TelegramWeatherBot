package org.example.telegramweatherbot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Slf4j
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
            int temperature = (int) temp;
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
                            "%s Температура: %d°C\n" +
                            "📝 Описание: %s\n" +
                            "💧 Влажность: %d%%\n" +
                            "💨 Ветер: %.1f км/ч, %s\n" +
                            "☁️ Облачность: %d%%\n" +
                            "☀️ УФ-индекс: %.1f\n" +
                            "%s",
                    cityName, region, country, localTime,
                    emoji, temperature, condition, humidity,
                    windKph, windDir, cloud, uvIndex, rainInfo);

        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Город не найден!\n\n" +
                    "Проверьте название города (на английском).\n" +
                    "Пример: Moscow, London, Kazan, Izhevsk";
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
            result.append("📅 Прогноз на неделю для ").append(cityName).append(":\n\n");

            for (var day : forecastDays) {
                String date = day.get("date").asText();
                double maxTemp = day.get("day").get("maxtemp_c").asDouble();
                int maximTemp = (int) maxTemp;
                double minTemp = day.get("day").get("mintemp_c").asDouble();
                int minimTemp = (int) minTemp;
                String condition = day.get("day").get("condition").get("text").asText();
                int chanceOfRain = day.get("day").get("daily_chance_of_rain").asInt();
                String emoji = getWeatherEmoji(condition);

                String rainEmoji = "";
                if (chanceOfRain > 30) {
                    rainEmoji = " 🌧️";
                }

                String formattedDate = formatDate(date);

                result.append("• ")
                        .append(formattedDate)
                        .append(": ")
                        .append(String.format("%d°C", maximTemp))
                        .append(" / ")
                        .append(String.format("%d°C", minimTemp))
                        .append(") ")
                        .append(emoji)
                        .append(" ")
                        .append(condition)
                        .append(rainEmoji)
                        .append("\n");
            }

            return result.toString();

        } catch (Exception e) {
            e.printStackTrace();
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

    public String getCityByCoords(double lat, double lon) {
        try {
            lat = Math.round(lat * 10000.0) / 10000.0;
            lon = Math.round(lon * 10000.0) / 10000.0;

            String url = BASE_URL + "current.json?key=" + API_KEY + "&q=" + lat + "," + lon + "&lang=ru";
            String response = restTemplate.getForObject(url, String.class);
            JsonNode json = mapper.readTree(response);

            String city = json.get("location").get("name").asText();
            String country = json.get("location").get("country").asText();

            if (!country.equals("Россия") && !country.equals("Russia")) {
                log.warn("Город {} находится в стране {}, возможно, ошибка геолокации", city, country);
                return null;
            }

            return city;
        } catch (Exception e) {
            log.error("Ошибка при определении города по координатам: {}", e.getMessage());
            return null;
        }
    }

    public String getHourlyForecast(String city) {
        try {
            String url = BASE_URL + "forecast.json?key=" + API_KEY + "&q=" + city + "&days=1&lang=ru";
            String response = restTemplate.getForObject(url, String.class);
            JsonNode json = mapper.readTree(response);

            String cityName = json.get("location").get("name").asText();
            var hours = json.get("forecast").get("forecastday").get(0).get("hour");

            StringBuilder result = new StringBuilder();
            result.append("📅 ПОГОДА НА СЕГОДНЯ для ").append(cityName).append(" (по часам):\n\n");

            for (int i = 0; i < 24; i++) {
                var hour = hours.get(i);
                String time = hour.get("time").asText().substring(11, 16);
                double temp = hour.get("temp_c").asDouble();
                int temperature = (int) temp;
                String condition = hour.get("condition").get("text").asText();
                String emoji = getWeatherEmoji(condition);
                int chanceOfRain = hour.get("chance_of_rain").asInt();

                String rainInfo = "";
                if (chanceOfRain > 30) {
                    rainInfo = " 🌧️" + chanceOfRain + "%";
                }

                result.append(emoji)
                        .append(" ")
                        .append(time)
                        .append(" → ")
                        .append(temperature)
                        .append("°C, ")
                        .append(condition)
                        .append(rainInfo)
                        .append("\n");
            }

            return result.toString();

        } catch (Exception e) {
            log.error("Ошибка при получении почасового прогноза: {}", e.getMessage());
            return "❌ Не удалось получить прогноз на день.";
        }
    }
}
