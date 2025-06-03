FROM openjdk:17-jdk-slim

COPY build/libs/AITemplate-1.0.0.jar app.jar
COPY src/main/resources/service-account.json /app/service-account.json

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]