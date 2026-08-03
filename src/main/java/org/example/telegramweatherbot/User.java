package org.example.telegramweatherbot;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Long chatId;

    private String city;

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public String getCity() {
        return city;
    }

    public Long getChatId() {
        return chatId;
    }

    public Long getId() {
        return id;
    }

    private LocalDateTime lastUpdate;

    public void setLastUpdate(LocalDateTime now) {
        this.lastUpdate = now;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }
    public void setCity(String city) {
        this.city = city;
    }
}
