FROM openjdk:17-jdk-slim
WORKDIR /app
COPY /build/libs/service.jar /app/service.jar
ENTRYPOINT ["java", "-jar", "service.jar"]