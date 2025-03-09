# Stage 1: Build the application
FROM openjdk:17-jdk-slim AS builder
LABEL authors="raguvaran"

# Set the working directory
WORKDIR /app

# Copy the source code
COPY . .

# Install Maven and build the application
RUN apt-get update && \
    apt-get install -y maven && \
    mvn clean install -DskipTests

RUN ls -l /app/target

# Stage 2: Create the final image
FROM openjdk:17-jdk-slim
LABEL authors="raguvaran"

# Set the working directory
WORKDIR /app

# Copy the built JAR file from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose the application port
EXPOSE 9292

# Set the entrypoint to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]