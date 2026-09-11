# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
# Download dependencies first for better caching
RUN mvn dependency:go-offline -B
COPY src ./src
# Build the JAR, skipping tests since they run in CI
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Create a non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
# Copy the built JAR from the build stage
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8081
# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]