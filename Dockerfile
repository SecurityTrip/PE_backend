FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

# Копирование gradle файлов
COPY gradlew .
COPY gradle gradle
COPY settings.gradle.kts .
COPY build.gradle.kts .

# Установка прав на gradlew
RUN chmod +x ./gradlew

# Скачивание всех зависимостей
RUN ./gradlew dependencies

# Копирование исходного кода
COPY src src

# Сборка приложения
RUN ./gradlew build -x test

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Копирование собранного jar-файла
COPY --from=build /app/build/libs/*.jar app.jar

# Порт, который будет использоваться приложением
EXPOSE 8080

# Запуск приложения
ENTRYPOINT ["java", "-jar", "app.jar"] 