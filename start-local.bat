@echo off
setlocal enabledelayedexpansion

REM Load .env file, ignoring comments
if exist .env (
    for /f "usebackq tokens=1,* delims==" %%a in (".env") do (
        set "key=%%a"
        if "!key:~0,1!" neq "#" (
            set "%%a=%%b"
        )
    )
) else (
    echo WARNING: .env file not found.
)


echo starting RabbitMQ (5762)...
start "RabbitMQ" cmd /k "rabbitmq-server start"
timeout /t 10 /nobreak > nul

echo Starting Discovery (8761)...
start "Discovery" cmd /k "java -jar discovery-server/target/discovery-server-0.0.1-SNAPSHOT.jar || (echo SERVER CRASHED! & pause)"
timeout /t 20 /nobreak > nul

echo Starting Config (8888)...
start "Config" cmd /k "java -jar config-server/target/config-server-0.0.1-SNAPSHOT.jar || (echo SERVER CRASHED! & pause)"
timeout /t 15 /nobreak > nul

echo Starting Gateway (8080)...
start "Gateway" cmd /k "java -jar api-gateway/target/api-gateway-0.0.1-SNAPSHOT.jar || (echo SERVER CRASHED! & pause)"
timeout /t 10 /nobreak > nul

echo Starting Identity (8081)...
start "Identity" cmd /k "java -jar identity-service/target/identity-service-0.0.1-SNAPSHOT.jar || (echo SERVER CRASHED! & pause)"
timeout /t 5 /nobreak > nul

echo Starting Loan (8082)...
start "Loan" cmd /k "java -jar loan-service/target/loan-service-0.0.1-SNAPSHOT.jar || (echo SERVER CRASHED! & pause)"
timeout /t 5 /nobreak > nul

echo Starting EMI (8083)...
start "EMI" cmd /k "java -jar emi-service/target/emi-service-0.0.1-SNAPSHOT.jar || (echo SERVER CRASHED! & pause)"
timeout /t 5 /nobreak > nul

echo Starting Payment (8084)...
start "Payment" cmd /k "java -jar payment-service/target/payment-service-0.0.1-SNAPSHOT.jar || (echo SERVER CRASHED! & pause)"
timeout /t 5 /nobreak > nul

echo Starting Notification (8085)...
start "Notification" cmd /k "java -jar notification-service/target/notification-service-0.0.1-SNAPSHOT.jar || (echo SERVER CRASHED! & pause)"

echo.
echo Done! Eureka: http://localhost:8761  API: http://localhost:8080
pause
