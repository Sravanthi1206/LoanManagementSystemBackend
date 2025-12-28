@echo off
REM ============================================
REM LMS Backend - Fully Local Startup Script
REM ============================================
REM This script:
REM 1. Builds all microservices (mvn clean package)
REM 2. Starts them using java -jar
REM ============================================

echo ========================================
echo    LMS Backend - Build & Run
echo ========================================
echo.

REM Navigate to script directory
cd /d "%~dp0"

REM Check if MySQL is running
echo [Checking] MySQL service...
sc query MySQL >nul 2>&1
if errorlevel 1 (
    sc query MySQL80 >nul 2>&1
    if errorlevel 1 (
        echo WARNING: MySQL service not found.
        echo Please ensure MySQL is installed and running.
        echo.
        choice /C YN /M "Continue anyway"
        if errorlevel 2 exit /b 1
    )
)
echo MySQL: OK

REM Check if MongoDB is running
echo [Checking] MongoDB service...
sc query MongoDB >nul 2>&1
if errorlevel 1 (
    echo WARNING: MongoDB service not found.
    echo Please ensure MongoDB is installed and running.
    choice /C YN /M "Continue anyway"
    if errorlevel 2 exit /b 1
)
echo MongoDB: OK

echo.
echo ========================================
echo    Phase 1: Building Services
echo ========================================
echo.
echo Running 'mvn clean package -DskipTests' in background...
echo This may take a few minutes...
call mvn clean package -DskipTests
if errorlevel 1 (
    echo BUILD FAILED! Aborting start.
    pause
    exit /b 1
)

echo.
echo ========================================
echo    Phase 2: Starting Services (JARs)
echo ========================================
echo.

echo [1/8] Starting Config Server (port 8888)...
start "Config Server" java -jar config-server/target/config-server-0.0.1-SNAPSHOT.jar
echo Waiting 30s for Config Server to fully initialize...
timeout /t 30 /nobreak >nul

echo [2/8] Starting Discovery Server (port 8761)...
start "Discovery Server" java -jar discovery-server/target/discovery-server-0.0.1-SNAPSHOT.jar
echo Waiting 20s for Discovery Server to register...
timeout /t 20 /nobreak >nul

echo [3/8] Starting API Gateway (port 8080)...
start "API Gateway" java -jar api-gateway/target/api-gateway-0.0.1-SNAPSHOT.jar
timeout /t 10 /nobreak >nul

echo [4/8] Starting Identity Service (port 8081)...
start "Identity Service" java -jar identity-service/target/identity-service-0.0.1-SNAPSHOT.jar
timeout /t 10 /nobreak >nul

echo [5/8] Starting Loan Service (port 8082)...
start "Loan Service" java -jar loan-service/target/loan-service-0.0.1-SNAPSHOT.jar
timeout /t 10 /nobreak >nul

echo [6/8] Starting EMI Service (port 8083)...
start "EMI Service" java -jar emi-service/target/emi-service-0.0.1-SNAPSHOT.jar
timeout /t 10 /nobreak >nul

echo [7/8] Starting Payment Service (port 8084)...
start "Payment Service" java -jar payment-service/target/payment-service-0.0.1-SNAPSHOT.jar
timeout /t 10 /nobreak >nul

echo [8/8] Starting Notification Service (port 8085)...
start "Notification Service" java -jar notification-service/target/notification-service-0.0.1-SNAPSHOT.jar
timeout /t 10 /nobreak >nul

echo.
echo ========================================
echo    All services started!
echo ========================================
echo.
echo Service URLs:
echo   - Eureka Dashboard: http://localhost:8761
echo   - API Gateway:      http://localhost:8080
echo   - Config Server:    http://localhost:8888
echo.
echo Database Connections (Local):
echo   - MySQL:   localhost:3306
echo   - MongoDB: localhost:27017
echo.
echo Test Credentials:
echo   - Customer: customer@lms.com / Password@123
echo   - Officer:  officer@lms.com  / Password@123
echo   - Admin:    admin@lms.com    / Password@123
echo.
echo Press any key to exit this window (services will keep running)...
pause >nul
