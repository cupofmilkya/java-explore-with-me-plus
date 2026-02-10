FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY . .

RUN mvn clean package -DskipTests -pl main-service -am

FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache netcat-openbsd
WORKDIR /app
COPY --from=build /app/main-service/target/main-service-*.jar app.jar
COPY wait-for-it.sh /usr/local/bin/wait-for-it
RUN chmod +x /usr/local/bin/wait-for-it

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "wait-for-it main-db:5432 -- wait-for-it stats-server:9090 -- java -jar app.jar"]