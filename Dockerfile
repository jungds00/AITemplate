FROM openjdk:17-jdk-slim

COPY build/libs/AITemplate-1.0.0.jar app.jar
COPY wait-for-it.sh /wait-for-it.sh
COPY src/main/resources/service-account.json /app/service-account.json
RUN chmod +x /wait-for-it.sh

EXPOSE 8080

ENTRYPOINT ["/wait-for-it.sh", "mysql:3306", "--timeout=60", "--strict", "--",
            "/wait-for-it.sh", "elasticsearch:9200", "--timeout=60", "--strict", "--",
            "/wait-for-it.sh", "redis:6379", "--timeout=60", "--strict", "--",
            "java", "-jar", "app.jar", "--spring.profiles.active=prod"]
