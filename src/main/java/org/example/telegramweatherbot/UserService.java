package org.example.telegramweatherbot;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User saveUser(Long chatId, String city) {
        User user = userRepository.findByChatId(chatId).orElse(new User());
        user.setChatId(chatId);
        user.setCity(city);
        user.setLastUpdate(LocalDateTime.now());
        return userRepository.save(user);
    }

    public String getCity(Long chatId) {
        return userRepository.findByChatId(chatId)
                .map(User::getCity)
                .orElse(null);
    }

    public boolean userExists(Long chatId) {
        return userRepository.findByChatId(chatId).isPresent();
    }
}
