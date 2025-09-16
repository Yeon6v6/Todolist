FROM openjdk:21-jdk-slim

WORKDIR /app

COPY build/libs/noti-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]