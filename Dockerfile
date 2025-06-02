FROM openjdk:17-jdk-slim

COPY build/libs/AITemplate-1.0.0.jar /app/app.jar

COPY wait-for-it.sh /app/wait-for-it.sh
COPY src/main/resources/service-account.json /app/service-account.json
RUN chmod +x /app/wait-for-it.sh

WORKDIR /app
EXPOSE 8080

ENTRYPOINT ["bash", "-c", \
  "./wait-for-it.sh mysql:3306 -t 60 -- \
   ./wait-for-it.sh elasticsearch:9200 -t 60 -- \
   ./wait-for-it.sh redis:6379 -t 60 -- \
   java -jar app.jar"]
