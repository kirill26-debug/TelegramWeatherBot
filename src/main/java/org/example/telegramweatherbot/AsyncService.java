package org.example.telegramweatherbot;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncService {

    private final UserRepository userRepository;
    private final TelegramClient telegramClient;  // ← добавили

    @Async
    public void toggleNotifications(Long chatId) {  // ← убрали UpdateConsumer
        try {
            User user = userRepository.findByChatId(chatId).orElse(null);
            if (user == null) {
                sendMessage(chatId, "❌ Пользователь не найден");
                return;
            }
            boolean current = user.isNotificationEnabled();
            user.setNotificationEnabled(!current);
            userRepository.save(user);

            String status = user.isNotificationEnabled() ? "включены ✅" : "отключены ❌";
            sendMessage(chatId, "🔔 Уведомления " + status);
            log.info("✅ Уведомления для {} переключены на {}", chatId, status);

        } catch (Exception e) {
            log.error("❌ Ошибка при переключении уведомлений: {}", e.getMessage());
            sendMessage(chatId, "❌ Ошибка при переключении: " + e.getMessage());
        }
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