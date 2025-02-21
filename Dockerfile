# Stage 1: Build the application
FROM maven:3.8.6-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Create a runtime image
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/auction-*.jar /app/auction.jar

# Expose the port your application runs on (default for Spring Boot is 8080)
EXPOSE 8080

# Set the entrypoint to run the application
ENTRYPOINT ["java", "-jar", "/app/auction.jar"]