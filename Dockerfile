# Stage 1: Build the application
FROM eclipse-temurin:17-jdk-alpine AS builder
LABEL authors="raguvaran"

RUN apk add --no-cache maven

# Set the working directory
WORKDIR /app

# Copy only the necessary files for building (pom.xml and src/)
COPY pom.xml .
COPY src ./src

# Build the application using Maven
RUN mvn clean install -DskipTests

# Stage 2: Create the final image
FROM eclipse-temurin:17-jre-alpine
LABEL authors="raguvaran"

# Set the working directory
WORKDIR /app

# Copy the built JAR file from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose the application port
EXPOSE 9292

# Set the entrypoint to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]