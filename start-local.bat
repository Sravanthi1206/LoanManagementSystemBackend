@echo off
REM Set environment variables directly (no .env parsing issues)

REM MySQL Database
set MYSQL_HOST=localhost
set MYSQL_PORT=3306
set MYSQL_USER=root
set MYSQL_PASSWORD=root

REM MongoDB
set MONGO_HOST=localhost
set MONGO_PORT=27017

REM RabbitMQ
set RABBITMQ_HOST=localhost
set RABBITMQ_PORT=5672
set RABBITMQ_USER=guest
set RABBITMQ_PASS=guest

REM JWT
set JWT_SECRET=5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437
set JWT_EXPIRATION=3600000

REM Email
set MAIL_HOST=smtp.gmail.com
set MAIL_PORT=587
set MAIL_USERNAME=sravanthigurram955@gmail.com
set MAIL_PASSWORD=blmvcdbtosuvhlzt

REM Eureka
set EUREKA_HOST=localhost
set EUREKA_PORT=8761

REM CORS
set CORS_ALLOWED_ORIGINS=http://localhost:4200

REM Root Admin
set ROOT_ADMIN_EMAIL=root@lms.com
set ROOT_ADMIN_PASSWORD=Password@123

echo Environment variables set!

echo.
echo Starting RabbitMQ (5672)...
start "RabbitMQ" cmd /k "rabbitmq-server start"
timeout /t 10 /nobreak > nul

echo Starting Discovery (8761)...
start "Discovery" java -jar discovery-server/target/discovery-server-0.0.1-SNAPSHOT.jar
timeout /t 20 /nobreak > nul

echo Starting Config (8888)...
start "Config" java -jar config-server/target/config-server-0.0.1-SNAPSHOT.jar
timeout /t 15 /nobreak > nul

echo Starting Gateway (8080)...
start "Gateway" java -jar api-gateway/target/api-gateway-0.0.1-SNAPSHOT.jar
timeout /t 10 /nobreak > nul

echo Starting Identity (8081)...
start "Identity" java -jar identity-service/target/identity-service-0.0.1-SNAPSHOT.jar
timeout /t 5 /nobreak > nul

echo Starting Loan (8082)...
start "Loan" java -jar loan-service/target/loan-service-0.0.1-SNAPSHOT.jar
timeout /t 5 /nobreak > nul

echo Starting EMI (8083)...
start "EMI" java -jar emi-service/target/emi-service-0.0.1-SNAPSHOT.jar
timeout /t 5 /nobreak > nul

echo Starting Payment (8084)...
start "Payment" java -jar payment-service/target/payment-service-0.0.1-SNAPSHOT.jar
timeout /t 5 /nobreak > nul

echo Starting Notification (8085)...
start "Notification" java -jar notification-service/target/notification-service-0.0.1-SNAPSHOT.jar

echo.
echo Done! Eureka: http://localhost:8761  API: http://localhost:8080
pause
