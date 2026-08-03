FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
RUN chmod +x mvnw

COPY pom.xml .
RUN ./mvnw dependency:go-offline

COPY src src

RUN ./mvnw clean package -DskipTests

EXPOSE 8080

ENTRYPOINT ["java", "-Dserver.port=8080", "-jar", "target/TelegramWeatherBot-0.0.1-SNAPSHOT.jar"]
