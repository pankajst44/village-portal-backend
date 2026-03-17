FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY target/village-portal-1.0.0.jar app.jar

# Create uploads directory for persistent storage
RUN mkdir -p /app/uploads

EXPOSE 8080

# Use production profile for cloud deployment
ENTRYPOINT ["java","-jar","-Dspring.profiles.active=prod","app.jar"]