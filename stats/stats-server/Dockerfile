FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /build

COPY pom.xml .
COPY stats ./stats
COPY main-service ./main-service

RUN mvn clean install -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /build/stats/stats-server/target/*jar app.jar

# Открываем оба порта: 9090 — основной API статистики, 8080 — заглушки main-сервиса
EXPOSE 9090
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]