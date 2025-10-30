# Multi-stage build for Spring Boot application
FROM maven:3.9.4-eclipse-temurin-21 AS build

# Set working directory
WORKDIR /app

# Copy pom.xml and download dependencies (for better caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:21-jdk-slim

# Install curl and mysql-client for health checks and database connectivity
RUN apt-get update && apt-get install -y curl default-mysql-client && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy the built jar from build stage
COPY --from=build /app/target/librivault-backend-1.0.0.jar app.jar

# Copy wait script
COPY wait-for-db.sh wait-for-db.sh
RUN chmod +x wait-for-db.sh

# Create non-root user for security
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/api/health || exit 1

# Run the application with database wait
ENTRYPOINT ["./wait-for-db.sh"]