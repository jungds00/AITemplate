FROM openjdk:17-jdk-slim

COPY build/libs/AITemplate-1.0.0.jar app.jar
COPY wait-for-it.sh /wait-for-it.sh
RUN chmod +x /wait-for-it.sh


EXPOSE 8080

ENTRYPOINT ["/wait-for-it.sh", "mysql:3306", "--", "/wait-for-it.sh", "elasticsearch:9200", "--", "/wait-for-it.sh", "redis:6379", "--", "java", "-jar", "app.jar"]
