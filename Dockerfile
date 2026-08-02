FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Копируем Maven wrapper
COPY mvnw .
COPY .mvn .mvn

# Копируем pom.xml и скачиваем зависимости
COPY pom.xml .
RUN ./mvnw dependency:go-offline

# Копируем исходники
COPY src src

# Собираем проект
RUN ./mvnw clean package -DskipTests

# Открываем порт
EXPOSE 8080

# Запускаем бота
ENTRYPOINT ["java", "-Dserver.port=8080", "-jar", "target/TelegramWeatherBot-0.0.1-SNAPSHOT.jar"]
