# ═══════════════════════════════════════════════════════════
# Multi-stage Dockerfile - Builds JAR during deployment
# ═══════════════════════════════════════════════════════════

# ── Stage 1: Build with Maven ──
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /build

# Copy pom.xml first for better caching
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application (skip tests for deployment)
RUN mvn clean package -DskipTests -B

# ── Stage 2: Runtime Image ──
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /build/target/village-portal-1.0.0.jar app.jar

# Create uploads directory for persistent storage
RUN mkdir -p /app/uploads

EXPOSE 8080

# Use production profile for cloud deployment
ENTRYPOINT ["java","-jar","-Dspring.profiles.active=prod","app.jar"]
