# Stage 1: Build stage
FROM maven:3.8.7-eclipse-temurin-17 AS build

# Set the working directory to /app
WORKDIR /app

# Copy the application source
COPY pom.xml .
COPY src ./src

# Clone the xrechnung-visualization repository as a sibling to /app
WORKDIR /

# Build the application
WORKDIR /app
RUN mvn clean package -DskipTests

# Stage 2: Final runtime stage
FROM eclipse-temurin:17-jdk

# Install git to retain the cloned repository in the final image
RUN apt-get update && apt-get install -y git && rm -rf /var/lib/apt/lists/*

# Set the working directory to /app
WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Clone the xrechnung-visualization repository in the final image
WORKDIR /
RUN git clone https://github.com/itplr-kosit/xrechnung-visualization.git

# Return to /app for execution
WORKDIR /app

# Expose the application port
EXPOSE 4711

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
