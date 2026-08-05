package org.example.telegramweatherbot;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

@Component
@Slf4j
public class UpdateConsumer implements LongPollingUpdateConsumer {

    private final TelegramClient telegramClient;
    private final WeatherService weatherService;
    private final UserService userService;
    private final AsyncService asyncService;

    private final java.util.Map<Long, Boolean> waitingForCity = new java.util.HashMap<>();

    public UpdateConsumer(TelegramClient telegramClient,
                          WeatherService weatherService,
                          UserService userService,
                          AsyncService asyncService) {
        this.telegramClient = telegramClient;
        this.weatherService = weatherService;
        this.userService = userService;
        this.asyncService = asyncService;
    }

    @SneakyThrows
    @Override
    public void consume(List<Update> updates) {
        for (Update update : updates) {
            if (update.hasMessage() && update.getMessage().hasLocation()) {
                Long chatId = update.getMessage().getChatId();
                double lat = update.getMessage().getLocation().getLatitude();
                double lon = update.getMessage().getLocation().getLongitude();

                String city = weatherService.getCityByCoords(lat, lon);

                if (city != null && !city.equals("Город не найден")) {
                    userService.saveUser(chatId, city);
                    sendMessage(chatId, "📍 Определён город: " + city);
                    sendMainMenu(chatId);
                } else {
                    sendMessage(chatId, "❌ Не удалось определить город. Попробуйте ввести его вручную через '🌍 Сменить город'");
                }
                continue;
            }

            if (update.hasMessage() && update.getMessage().hasText()) {
                String messageText = update.getMessage().getText();
                Long chatId = update.getMessage().getChatId();

                System.out.printf("Пришло сообщение '%s' от %d%n", messageText, chatId);

                if (waitingForCity.getOrDefault(chatId, false)) {
                    userService.saveUser(chatId, messageText);
                    waitingForCity.put(chatId, false);
                    sendMessage(chatId, "✅ Город сохранен: " + messageText);
                    sendMainMenu(chatId);
                    return;
                }

                switch (messageText) {
                    case "/start" -> sendMainMenu(chatId);
                    case "/keyboard" -> sendReplayKeyboard(chatId);

                    case "/weather", "🌤 Погода сегодня" -> sendWeatherToday(chatId);
                    case "/forecast", "📅 Прогноз на неделю" -> sendWeeklyForecast(chatId);
                    case "/city", "🌍 Сменить город" -> sendChangeCity(chatId);
                    case "/help", "❓ Помощь" -> sendHelpUser(chatId);
                    case "/reset", "♾ Сброс настроек" -> sendResetSettings(chatId);
                    case "/donate_to_author","❤️ Поддержать автора" -> sendDonateToAuthor(chatId);
                    case "/notifications","🔔 Уведомления" -> sendNotifications(chatId);

                    default -> sendMessage(chatId, "Я вас не понимаю! Используйте кнопки меню.");
                }
            }
            else if (update.hasCallbackQuery()) {
                handleCallbackQuery(update.getCallbackQuery());
            }
        }
    }

    @SneakyThrows
    private void sendReplayKeyboard(Long chatId) {
        KeyboardRow row1 = new KeyboardRow();
        row1.add("🌤 Погода сегодня");
        row1.add("📅 Прогноз на неделю");

        KeyboardRow row2 = new KeyboardRow();
        KeyboardButton locationButton = KeyboardButton.builder()
                .text("📍 Отправить геолокацию")
                .requestLocation(true)
                .build();
        row2.add(locationButton);
        row2.add("🔔 Уведомления");

        KeyboardRow row3 = new KeyboardRow();
        row3.add("🌍 Сменить город");
        row3.add("❓ Помощь");

        KeyboardRow row4 = new KeyboardRow();
        row4.add("♾ Сброс настроек");
        row4.add("❤️ Поддержать автора");

        ReplyKeyboardMarkup replyKeyboard = ReplyKeyboardMarkup.builder()
                .keyboard(List.of(row1, row2, row3, row4))
                .resizeKeyboard(true)
                .oneTimeKeyboard(false)
                .build();

        SendMessage message = SendMessage.builder()
                .chatId(chatId.toString())
                .replyMarkup(replyKeyboard)
                .build();

        telegramClient.execute(message);
    }

    private void answerCallback(CallbackQuery callbackQuery) {
        try {
            telegramClient.execute(
                    AnswerCallbackQuery.builder()
                            .callbackQueryId(callbackQuery.getId())
                            .text("✅")
                            .build()
            );
        } catch (Exception e) {
            log.error("Ошибка при ответе на callback: {}", e.getMessage());
        }
    }


    private void handleCallbackQuery(CallbackQuery callbackQuery) {
        answerCallback(callbackQuery);

        String data = callbackQuery.getData();
        Long chatId = callbackQuery.getFrom().getId();

        switch (data) {
            case "weather_today" -> sendWeatherToday(chatId);
            case "weekly_forecast" -> sendWeeklyForecast(chatId);
            case "change_city" -> sendChangeCity(chatId);
            case "help_user" -> sendHelpUser(chatId);
            case "reset_settings" -> sendResetSettings(chatId);
            case "donate_to_author" -> sendDonateToAuthor(chatId);
            case "notifications" -> sendNotifications(chatId);
            default -> sendMessage(chatId, "Неизвестная команда");
        }
    }

    private void sendNotifications(Long chatId) {
        sendMessage(chatId, "⏳ Переключаю статус уведомлений...");
        asyncService.toggleNotifications(chatId);
    }

    @SneakyThrows
    private void sendDonateToAuthor(Long chatId) {
        sendMessage(chatId, """
        ❤️ Поддержать разработку
        
        🌐 Нажми на ссылку ниже, чтобы перейти к оплате:
        👇👇👇
        https://www.donationalerts.com/r/mikri001
        
        🔥 Спасибо, что помогаешь развивать бота! 🔥
        """);
    }

    @SneakyThrows
    private void sendMessage(Long chatId, String messageText) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId.toString())
                .text(messageText)
                .build();
        telegramClient.execute(message);
    }

    @SneakyThrows
    private void sendResetSettings(Long chatId) {
        userService.saveUser(chatId, null);
        waitingForCity.put(chatId, false);

        sendMessage(chatId, """
            ♾ Настройки сброшены!
            
            ✅ Город удалён
            ✅ Все настройки очищены
            
            Чтобы начать, нажмите "🌍 Сменить город" и введите ваш город.
            """);
    }

    @SneakyThrows
    private void sendHelpUser(Long chatId) {
        sendMessage(chatId, """
                ❓ Помощь:
                
                🌤 Погода сегодня - прогноз на сегодня
                📅 Прогноз на неделю - прогноз на 7 дней
                🌍 Сменить город - изменить город
                ♾ Сброс настроек - удалить город и сбросить настройки
                🔔 Уведомления - включить/выключить
                📍 Отправить геолокацию - ваше расположение
                """);
    }

    @SneakyThrows
    private void sendChangeCity(Long chatId) {
        waitingForCity.put(chatId, true);
        sendMessage(chatId, "🌍 Напишите название города на английском:\n\nПример: Moscow, London, Kazan, Izhevsk");
    }

    @SneakyThrows
    private void sendWeeklyForecast(Long chatId) {
        String city = userService.getCity(chatId);
        if (city == null) {
            sendMessage(chatId, "🌍 Сначала сохраните город через кнопку '🌍 Сменить город'");
            return;
        }
        String forecast = weatherService.getWeeklyForecast(city);
        sendMessage(chatId, forecast);
    }

    @SneakyThrows
    private void sendWeatherToday(Long chatId) {
        String city = userService.getCity(chatId);
        if (city == null) {
            sendMessage(chatId, "🌍 Сначала сохраните город через кнопку '🌍 Сменить город'");
            return;
        }
        String weather = weatherService.getCurrentWeather(city);
        sendMessage(chatId, weather);
    }

    @SneakyThrows
    private void sendMainMenu(Long chatId) {
        sendReplayKeyboard(chatId);
    }
}