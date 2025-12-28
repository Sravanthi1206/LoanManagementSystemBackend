@echo off
REM LMS Backend Stop Script for Windows
REM Stops all running Java processes for LMS

echo ========================================
echo    LMS Backend Stop Script
echo ========================================
echo.

echo Stopping all Spring Boot services...

REM Find and kill Java processes related to LMS
for /f "tokens=1" %%i in ('wmic process where "name='java.exe' and commandline like '%%spring-boot%%'" get processid 2^>nul ^| findstr /r "[0-9]"') do (
    echo Stopping process %%i...
    taskkill /PID %%i /F >nul 2>&1
)

echo.
echo All services stopped.
echo.

set /p stopDocker="Stop Docker containers too? (y/n): "
if /i "%stopDocker%"=="y" (
    echo Stopping Docker containers...
    docker-compose down
    echo Docker containers stopped.
)

echo.
echo Done!
pause
