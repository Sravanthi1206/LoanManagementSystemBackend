@echo off
REM ============================================
REM LMS Backend - Stop All Services
REM ============================================
REM This script stops all running LMS services
REM ============================================

echo ========================================
echo    Stopping LMS Backend Services
echo ========================================
echo.

REM Kill all Java processes with LMS service names
taskkill /FI "WINDOWTITLE eq Config Server*" /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq Discovery Server*" /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq API Gateway*" /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq Identity Service*" /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq Loan Service*" /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq EMI Service*" /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq Payment Service*" /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq Notification Service*" /F >nul 2>&1

echo All LMS services have been stopped.
echo.
pause
