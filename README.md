# ☀️ Telegram Weather Bot

**Telegram Weather Bot** — это удобный и красивый бот для получения актуальной информации о погоде в любом городе мира.
---

## ✨ Возможности

- 🌤 **Текущая погода** — температура, влажность, ветер, облачность, УФ-индекс
- 📅 **Прогноз на день** — почасовая погода с текущего часа до полуночи
- 📅 **Прогноз на неделю** — погода на 7 дней вперед
- 📍 **Определение города по геолокации** — бот сам определит твой город
- 🌍 **Смена города** — быстрая смена локации
- 🔔 **Утренние уведомления** — рассылка погоды каждое утро (вкл/выкл)
- 🎨 **Красивые эмодзи** — понятное отображение погоды
- ❤️ **Поддержка автора** — кнопка для доната

---

## 🖼️ Скриншоты

| Главное меню | Текущая погода |
|--------------|----------------|
| ![Главное меню](images/image_1.jpg) | ![Текущая погода](images/image_2.jpg) |

| Прогноз на день | Прогноз на неделю |
|----------------|-------------------|
| ![Прогноз на день](images/image_3.jpg) | ![Прогноз на неделю](images/image_3.jpg) |

---

## 🛠️ Технологии

- **Java 21** — язык программирования
- **Spring Boot 4.1.0** — фреймворк
- **Spring Data JPA** — работа с базой данных
- **PostgreSQL** — хранение пользователей и городов
- **Telegram Bot API** — взаимодействие с Telegram
- **WeatherAPI.com** — получение данных о погоде
- **Docker** — контейнеризация
---

## 🚀 Запуск и деплой

### 🔧 Локальный запуск

1. Клонируй репозиторий:
   ```bash
   git clone https://github.com/kirill26-debug/TelegramWeatherBot.git
   cd TelegramWeatherBot
Создай файл .env с переменными:

env
BOT_TOKEN=твой_токен_бота
WEATHER_API_KEY=твой_ключ_от_WeatherAPI
DATABASE_URL=postgresql://user:pass@host:5432/db
Запусти приложение:

bash
./mvnw clean package
java -jar target/TelegramWeatherBot-0.0.1-SNAPSHOT.jar
🐳 Запуск через Docker
bash
docker build -t weather-bot .
docker run -d -p 8080:8080 --env-file .env weather-bot
🌐 Деплой на Render
Залей код на GitHub.

Создай Web Service на Render.

Подключи репозиторий и добавь переменные окружения.

Нажми Deploy.

📱 Команды бота
Команда	Описание
/start	Запустить бота и показать главное меню
/weather	Показать текущую погоду
/forecast	Показать прогноз на неделю
/city	Сменить город
/help	Показать помощь
/reset	Сбросить настройки
/notifications	Включить/выключить уведомления
📊 Схема базы данных
sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    chat_id BIGINT UNIQUE NOT NULL,
    city VARCHAR(255),
    last_update TIMESTAMP,
    notification_enabled BOOLEAN DEFAULT TRUE
);

GitHub: kirill26-debug
