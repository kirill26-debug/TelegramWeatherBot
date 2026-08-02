# ☀️ Telegram Weather Bot

[![Telegram](https://img.shields.io/badge/Telegram-@WeatherBot-blue)](https://t.me/your_bot)
[![Java](https://img.shields.io/badge/Java-21-orange)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-green)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-yellow)](LICENSE)

---

## 🌤️ О боте

**Telegram Weather Bot** — это удобный бот для получения актуальной информации о погоде в любом городе мира.

---

## ✨ Возможности

- 🌤️ **Текущая погода** — температура, влажность, ветер, облачность
- 📅 **Прогноз на неделю** — погода на 7 дней вперед
- 🌍 **Смена города** — быстрая смена локации
- 🎨 **Красивые эмодзи** — понятное отображение погоды
- ❤️ **Поддержка автора** — кнопка для доната

---

## 🛠️ Технологии

- **Java 21** — язык программирования
- **Spring Boot 3.2.0** — фреймворк
- **Telegram Bot API** — взаимодействие с Telegram
- **WeatherAPI.com** — получение данных о погоде
- **Docker** — контейнеризация
- **Maven** — сборка проекта

---

## 🤖 Команды бота

| Команда | Описание |
|---------|----------|
| `/start` | Запустить бота и показать главное меню |
| `/weather` | Показать погоду сегодня |
| `/forecast` | Показать прогноз на неделю |
| `/city` | Сменить город |
| `/help` | Показать помощь |
| `/reset` | Сбросить настройки |

---

## 📦 Установка и запуск

### 🔧 Локальный запуск

```bash
git clone https://github.com/kirill26-debug/TelegramWeatherBot.git
cd TelegramWeatherBot
./mvnw clean install
java -jar target/TelegramWeatherBot-0.0.1-SNAPSHOT.jar

🐳 Запуск через Docker
bash
docker build -t weather-bot .
docker run -d -p 8080:8080 --env-file .env weather-bot

🔧 Переменные окружения
Создайте файл .env в корне проекта:

env
BOT_TOKEN=ваш_токен_бота
WEATHER_API_KEY=ваш_api_ключ
PORT=8080

🌐 Деплой на Render
Создайте аккаунт на render.com

Нажмите New → Web Service

Подключите GitHub репозиторий

Выберите Language: Docker

Добавьте переменные окружения

Нажмите Create Web Service

📱 Как пользоваться
Напишите боту команду /start

Нажмите "🌍 Сменить город"

Введите название города на английском

Наслаждайтесь погодой! 🌤️

❤️ Поддержать автора
Если бот вам понравился, вы можете поддержать разработку:

💳 Сбер: 2202 2088 6967 4698

💳 Т-Банк: 2200 7020 7854 3555

📱 Telegram: @mikri001

