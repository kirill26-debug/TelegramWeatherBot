package org.example.telegramweatherbot;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

@Slf4j
@Component
public class ScheduledService {

    private final UserRepository userRepository;
    private final WeatherService weatherService;
    private final TelegramClient telegramClient;

    // Конструктор с внедрением зависимостей и токена
    public ScheduledService(UserRepository userRepository,
                            WeatherService weatherService,
                            @Value("${bot.token}") String botToken) {
        this.userRepository = userRepository;
        this.weatherService = weatherService;
        this.telegramClient = new OkHttpTelegramClient(botToken);
    }

    @Scheduled(cron = "0 0 8 * * *", zone = "Europe/Moscow")
    public void sendMorningWeather() {
        log.info("🌅 Начинаем утреннюю рассылку...");

        List<User> users = userRepository.findAll();

        for (User user : users) {
            try {
                String city = user.getCity();
                String weather = weatherService.getCurrentWeather(city);
                sendMessage(user.getChatId(), weather);
                log.info("✅ Отправлено пользователю {}", user.getChatId());
            } catch (Exception e) {
                log.error("❌ Ошибка для пользователя {}: {}", user.getChatId(), e.getMessage());
            }
        }

        log.info("✅ Рассылка завершена. Отправлено {} пользователям", users.size());
    }

    @SneakyThrows
    private void sendMessage(Long chatId, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId.toString())
                .text(text)
                .build();
        telegramClient.execute(message);
    }
}