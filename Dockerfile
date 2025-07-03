# Use Eclipse Temurin JDK 21 as base image
FROM eclipse-temurin:21-jdk as builder

# Set working directory
WORKDIR /app

# Copy Maven wrapper and project files
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

# Copy the rest of the project
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests

# Use a minimal JDK 21 runtime image
FROM eclipse-temurin:21-jdk as runner

# Set working directory in container
WORKDIR /app

# Copy the built jar from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose the port Spring Boot runs on
EXPOSE 9093

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
