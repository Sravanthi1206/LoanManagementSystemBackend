@echo off
setlocal EnableDelayedExpansion

echo ========================================
echo   LMS Backend - Local Startup Script
echo ========================================
echo.

:: Load environment variables from .env file
if exist .env (
    echo Loading environment variables from .env...
    for /f "usebackaliases tokens=1,2 delims==" %%a in (.env) do (
        set "%%a=%%b"
    )
)

:: Set default ports
set DISCOVERY_PORT=8761
set CONFIG_PORT=8888
set GATEWAY_PORT=8080
set IDENTITY_PORT=8081
set LOAN_PORT=8082
set EMI_PORT=8083
set PAYMENT_PORT=8084
set NOTIFICATION_PORT=8085

echo.
echo Starting services in order...
echo.

:: Start Discovery Server
echo [1/8] Starting Discovery Server on port %DISCOVERY_PORT%...
start "Discovery Server" cmd /c "cd discovery-server && mvn spring-boot:run"
timeout /t 15 /nobreak > nul

:: Start Config Server
echo [2/8] Starting Config Server on port %CONFIG_PORT%...
start "Config Server" cmd /c "cd config-server && mvn spring-boot:run"
timeout /t 10 /nobreak > nul

:: Start API Gateway
echo [3/8] Starting API Gateway on port %GATEWAY_PORT%...
start "API Gateway" cmd /c "cd api-gateway && mvn spring-boot:run"
timeout /t 10 /nobreak > nul

:: Start Identity Service
echo [4/8] Starting Identity Service on port %IDENTITY_PORT%...
start "Identity Service" cmd /c "cd identity-service && mvn spring-boot:run"
timeout /t 10 /nobreak > nul

:: Start Loan Service
echo [5/8] Starting Loan Service on port %LOAN_PORT%...
start "Loan Service" cmd /c "cd loan-service && mvn spring-boot:run"
timeout /t 5 /nobreak > nul

:: Start EMI Service
echo [6/8] Starting EMI Service on port %EMI_PORT%...
start "EMI Service" cmd /c "cd emi-service && mvn spring-boot:run"
timeout /t 5 /nobreak > nul

:: Start Payment Service
echo [7/8] Starting Payment Service on port %PAYMENT_PORT%...
start "Payment Service" cmd /c "cd payment-service && mvn spring-boot:run"
timeout /t 5 /nobreak > nul

:: Start Notification Service
echo [8/8] Starting Notification Service on port %NOTIFICATION_PORT%...
start "Notification Service" cmd /c "cd notification-service && mvn spring-boot:run"

echo.
echo ========================================
echo   All services started!
echo ========================================
echo.
echo Service URLs:
echo   Discovery:    http://localhost:%DISCOVERY_PORT%
echo   Config:       http://localhost:%CONFIG_PORT%
echo   Gateway:      http://localhost:%GATEWAY_PORT%
echo   Identity:     http://localhost:%IDENTITY_PORT%
echo   Loan:         http://localhost:%LOAN_PORT%
echo   EMI:          http://localhost:%EMI_PORT%
echo   Payment:      http://localhost:%PAYMENT_PORT%
echo   Notification: http://localhost:%NOTIFICATION_PORT%
echo.
pause
