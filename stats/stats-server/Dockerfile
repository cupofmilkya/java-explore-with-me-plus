FROM maven:3.9.9-eclipse-temurin-11 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/*jar app.jar
EXPOSE 9090
ENTRYPOINT ["java", "-jar", "app.jar"]