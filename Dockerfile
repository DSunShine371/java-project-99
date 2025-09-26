# ----------------------------------------------------------------------
# ЭТАП 1: СБОРКА (BUILDER STAGE)
FROM gradle:8.7-jdk21 AS builder
WORKDIR /app

# Оптимизированное копирование для кэширования слоев:
COPY build.gradle.kts settings.gradle.kts gradlew ./
COPY gradle ./gradle

# Копируем исходный код и фронтенд-файлы (без node_modules, благодаря .dockerignore)
COPY src ./src
COPY package.json package-lock.json ./

# Выполняем полную сборку (запустится Gradle, который запустит npm ci и сборку фронтенда)
RUN ./gradlew bootJar --no-daemon

# ----------------------------------------------------------------------
# ЭТАП 2: ПРОДАКШЕН (RUNNER STAGE)
# Легковесный JRE-образ для запуска
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Копируем ТОЛЬКО готовый исполняемый JAR
COPY --from=builder /app/build/libs/*.jar app.jar

# Запуск приложения без Gradle!
CMD ["java", "-jar", "app.jar"]