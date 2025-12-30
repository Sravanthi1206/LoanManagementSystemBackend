@echo off
REM ============================================
REM Build All JARs and Docker Images
REM ============================================
REM This script builds all microservice JARs using Maven,
REM then builds Docker images using the pre-built JARs.
REM ============================================

echo ============================================
echo LMS - Build All Services
echo ============================================

REM Check if Maven is available
where mvn >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo ERROR: Maven is not installed or not in PATH
    echo Please install Maven and try again
    pause
    exit /b 1
)

REM Navigate to Backend directory
cd /d "%~dp0"

echo.
echo Step 1: Building all JARs with Maven...
echo ============================================
call mvn clean package -DskipTests
if %ERRORLEVEL% neq 0 (
    echo ERROR: Maven build failed!
    pause
    exit /b 1
)

echo.
echo Step 2: Building Docker images...
echo ============================================
docker-compose build --no-cache
if %ERRORLEVEL% neq 0 (
    echo ERROR: Docker build failed!
    pause
    exit /b 1
)

echo.
echo ============================================
echo SUCCESS: All services built successfully!
echo ============================================
echo.
echo To start the services, run:
echo   docker-compose up -d
echo.
pause
