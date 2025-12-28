@echo off
REM LMS Backend Startup Script for Windows
REM This script starts all microservices in the correct order

echo ========================================
echo    LMS Backend Startup Script
echo ========================================
echo.

REM Check if Docker is running for databases
echo [1/8] Checking Docker for databases...
docker ps >nul 2>&1
if errorlevel 1 (
    echo WARNING: Docker is not running. Please start Docker Desktop first.
    echo          Run: docker-compose up -d
    pause
    exit /b 1
)

REM Check if databases are running
docker ps | findstr lms-mysql >nul 2>&1
if errorlevel 1 (
    echo Starting databases with Docker Compose...
    docker-compose up -d mysql mongodb
    echo Waiting 30 seconds for databases to start...
    timeout /t 30 /nobreak >nul
) else (
    echo Databases already running.
)

echo.
echo [2/8] Starting Config Server (port 8888)...
cd Backend\config-server
start "Config Server" cmd /c "mvn spring-boot:run"
timeout /t 20 /nobreak >nul
cd ..\..

echo [3/8] Starting Discovery Server (port 8761)...
cd Backend\discovery-server
start "Discovery Server" cmd /c "mvn spring-boot:run"
timeout /t 15 /nobreak >nul
cd ..\..

echo [4/8] Starting API Gateway (port 8080)...
cd Backend\api-gateway
start "API Gateway" cmd /c "mvn spring-boot:run"
timeout /t 10 /nobreak >nul
cd ..\..

echo [5/8] Starting Identity Service (port 8081)...
cd Backend\identity-service
start "Identity Service" cmd /c "mvn spring-boot:run"
timeout /t 10 /nobreak >nul
cd ..\..

echo [6/8] Starting Loan Service (port 8082)...
cd Backend\loan-service
start "Loan Service" cmd /c "mvn spring-boot:run"
timeout /t 10 /nobreak >nul
cd ..\..

echo [7/8] Starting EMI Service (port 8083)...
cd Backend\emi-service
start "EMI Service" cmd /c "mvn spring-boot:run"
timeout /t 10 /nobreak >nul
cd ..\..

echo [8/8] Starting Payment Service (port 8084)...
cd Backend\payment-service
start "Payment Service" cmd /c "mvn spring-boot:run"
timeout /t 5 /nobreak >nul
cd ..\..

REM Optional: Start Notification Service
REM echo Starting Notification Service (port 8085)...
REM cd Backend\notification-service
REM start "Notification Service" cmd /c "mvn spring-boot:run"
REM cd ..\..

echo.
echo ========================================
echo    All services are starting!
echo ========================================
echo.
echo Service URLs:
echo   - Eureka Dashboard: http://localhost:8761
echo   - API Gateway:      http://localhost:8080
echo   - Config Server:    http://localhost:8888
echo.
echo Test Credentials:
echo   - Customer: customer@lms.com / Password@123
echo   - Officer:  officer@lms.com  / Password@123
echo   - Admin:    admin@lms.com    / Password@123
echo.
echo Press any key to exit...
pause >nul
