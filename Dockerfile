# ── Stage 1: Build ─────────────────────────────────────────────
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

COPY gradle/ gradle/
COPY gradlew build.gradle settings.gradle ./
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon || true

COPY src/ src/
RUN ./gradlew bootJar --no-daemon -x test

# ── Stage 2: Runtime ───────────────────────────────────────────
FROM eclipse-temurin:21-jre
RUN apt-get update && apt-get install -y libgomp1 && rm -rf /var/lib/apt/lists/*
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar
COPY models/ models/

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
