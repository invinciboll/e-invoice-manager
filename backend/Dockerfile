# backend/Dockerfile
FROM maven:3.8.7-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM openjdk:17-jdk
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 4711
ENTRYPOINT ["java", "-jar", "app.jar"]
