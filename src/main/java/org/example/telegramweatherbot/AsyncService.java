package org.example.telegramweatherbot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncService {

    private final UserRepository userRepository;
    private final WeatherService weatherService;

    @Async
    public void notifications(Long chatId, UpdateConsumer updateConsumer) {
        try {
            User user = userRepository.findByChatId(chatId).orElse(null);
            if (user == null) {
                updateConsumer.sendMessage(chatId, "❌ Пользователь не найден");
                return;
            }
            boolean current = user.isNotificationEnabled();
            user.setNotificationEnabled(!current);
            userRepository.save(user);

            String status = user.isNotificationEnabled() ? "включены ✅" : "отключены ❌";
            updateConsumer.sendMessage(chatId, "🔔 Уведомления " + status);
            log.info("✅ Уведомления для {} переключены на {}", chatId, status);

        } catch (Exception e) {
            log.error("❌ Ошибка при переключении уведомлений: {}", e.getMessage());
            updateConsumer.sendMessage(chatId, "❌ Ошибка при переключении: " + e.getMessage());
        }
    }

    @Async
    public void sendWeatherAsync(Long chatId, String city, UpdateConsumer updateConsumer) {
        try {
            String weather = weatherService.getCurrentWeather(city);
            updateConsumer.sendMessage(chatId, weather);
        } catch (Exception e) {
            log.error("❌ Ошибка при отправке погоды: {}", e.getMessage());
            updateConsumer.sendMessage(chatId, "❌ Не удалось получить погоду");
        }
    }
}