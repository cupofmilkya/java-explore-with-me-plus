FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY main-service/pom.xml ./main-service/
RUN mvn dependency:go-offline -B

COPY main-service ./main-service
RUN mvn package -DskipTests -pl main-service -am

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/main-service/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]