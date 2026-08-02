package org.example.telegramweatherbot;

import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class UpdateConsumer implements LongPollingUpdateConsumer {

    private TelegramClient telegramClient;

    @Value("${bot.token}")
    private String botToken;

    private final WeatherService weatherService = new WeatherService();
    private final Map<Long, String> userCities = new HashMap<>();
    private final Map<Long, Boolean> waitingForCity = new HashMap<>();

    @PostConstruct
    public void init() {
        this.telegramClient = new OkHttpTelegramClient(botToken);
    }

    @SneakyThrows
    @Override
    public void consume(List<Update> updates) {
        for (Update update : updates) {
            if (update.hasMessage() && update.getMessage().hasText()) {
                String messageText = update.getMessage().getText();
                Long chatId = update.getMessage().getChatId();

                System.out.printf("Пришло сообщение '%s' от %d%n", messageText, chatId);

                if (waitingForCity.getOrDefault(chatId, false)) {
                    userCities.put(chatId, messageText);
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
                    case "/donate_to_author","Поддержать автора" -> sendDonateToAuthor(chatId);

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
        row2.add("🌍 Сменить город");
        row2.add("❓ Помощь");

        KeyboardRow row3 = new KeyboardRow();
        row3.add("♾ Сброс настроек");
        row3.add("Поддержать автора");

        ReplyKeyboardMarkup replyKeyboard = ReplyKeyboardMarkup.builder()
                .keyboard(List.of(row1, row2, row3))
                .resizeKeyboard(true)
                .oneTimeKeyboard(false)
                .build();

        SendMessage message = SendMessage.builder()
                .chatId(chatId.toString())
                .text("Это пример обычной клавиатуры! 👇")
                .replyMarkup(replyKeyboard)
                .build();

        telegramClient.execute(message);
    }

    private void handleCallbackQuery(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        Long chatId = callbackQuery.getFrom().getId();

        System.out.printf("Нажата inline кнопка: '%s' от %d%n", data, chatId);

        switch (data) {
            case "weather_today" -> sendWeatherToday(chatId);
            case "weekly_forecast" -> sendWeeklyForecast(chatId);
            case "change_city" -> sendChangeCity(chatId);
            case "help_user" -> sendHelpUser(chatId);
            case "reset_settings" -> sendResetSettings(chatId);
            case "donate_to_author" -> sendDonateToAuthor(chatId);
            default -> sendMessage(chatId, "Неизвестная команда");
        }
    }

    @SneakyThrows
    private void sendDonateToAuthor(Long chatId) {
        sendMessage(chatId, """
            ❤️ Спасибо за поддержку!
            
            ☕ Поддержать разработку:
            • Сбер: 2202 2088 6967 4698
            • Т-Банк: 2200 7020 7854 3555
            
            📱 Связаться со мной:
            • Telegram: @mikri001
            
            🔥 Бот бесплатный! Спасибо, что пользуетесь! ❤️
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
        userCities.remove(chatId);
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
                """);
    }

    @SneakyThrows
    private void sendChangeCity(Long chatId) {
        waitingForCity.put(chatId, true);
        sendMessage(chatId, "🌍 Напишите название города на английском:\n\nПример: Moscow, London, Kazan, Izhevsk");
    }

    @SneakyThrows
    private void sendWeeklyForecast(Long chatId) {
        if (!userCities.containsKey(chatId)) {
            sendMessage(chatId, "🌍 Сначала сохраните город через кнопку '🌍 Сменить город'");
            return;
        }

        String city = userCities.get(chatId);
        String forecast = weatherService.getWeeklyForecast(city);
        sendMessage(chatId, forecast);
    }

    @SneakyThrows
    private void sendWeatherToday(Long chatId) {
        if (!userCities.containsKey(chatId)) {
            sendMessage(chatId, "🌍 Сначала сохраните город через кнопку '🌍 Сменить город'");
            return;
        }

        String city = userCities.get(chatId);
        String weather = weatherService.getCurrentWeather(city);
        sendMessage(chatId, weather);
    }

    @SneakyThrows
    private void sendMainMenu(Long chatId) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId.toString())
                .text("Добро пожаловать! Выберите действие:")
                .build();

        InlineKeyboardButton button1 = InlineKeyboardButton.builder()
                .text("🌤 Погода сегодня")
                .callbackData("weather_today")
                .build();

        InlineKeyboardButton button2 = InlineKeyboardButton.builder()
                .text("📅 Прогноз на неделю")
                .callbackData("weekly_forecast")
                .build();

        InlineKeyboardButton button3 = InlineKeyboardButton.builder()
                .text("🌍 Сменить город")
                .callbackData("change_city")
                .build();

        InlineKeyboardButton button4 = InlineKeyboardButton.builder()
                .text("❓ Помощь")
                .callbackData("help_user")
                .build();

        InlineKeyboardButton button5 = InlineKeyboardButton.builder()
                .text("♾ Сброс настроек")
                .callbackData("reset_settings")
                .build();

        InlineKeyboardButton button6 = InlineKeyboardButton.builder()
                .text("❤️ Поддержать автора")
                .callbackData("donate_to_author")
                .build();

        List<InlineKeyboardRow> keyboardRows = List.of(
                new InlineKeyboardRow(button1, button2),
                new InlineKeyboardRow(button3, button4),
                new InlineKeyboardRow(button5, button6)
        );

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(keyboardRows);
        message.setReplyMarkup(markup);

        telegramClient.execute(message);
    }
}