FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY . .

RUN mvn clean package -DskipTests -pl stats/stats-server -am

FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache netcat-openbsd
WORKDIR /app
COPY --from=build /app/stats/stats-server/target/stats-server-*.jar app.jar

EXPOSE 9090

ENTRYPOINT ["wait-for-it", "stats-db:5432", "--", "java", "-jar", "app.jar"]