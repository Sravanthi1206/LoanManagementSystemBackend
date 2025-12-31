# Lightweight runtime image - JAR must be pre-built with: mvn clean package -DskipTests
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar

# Create uploads directory for document storage
RUN mkdir -p /app/uploads

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
