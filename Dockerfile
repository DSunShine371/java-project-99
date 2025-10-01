# ----------------------------------------------------------------------
FROM gradle:8.7-jdk21 AS builder
WORKDIR /app

COPY build.gradle.kts settings.gradle.kts gradlew ./
COPY gradle ./gradle

COPY src ./src
COPY package.json package-lock.json ./

RUN ./gradlew bootJar --no-daemon

# ----------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 10000

CMD ["java", "-jar", "app.jar"]