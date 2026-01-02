@echo off
echo Stopping all LMS Microservices...

taskkill /FI "WINDOWTITLE eq Config Server" /F
taskkill /FI "WINDOWTITLE eq Discovery Server" /F
taskkill /FI "WINDOWTITLE eq API Gateway" /F
taskkill /FI "WINDOWTITLE eq Identity Service" /F
taskkill /FI "WINDOWTITLE eq Loan Service" /F
taskkill /FI "WINDOWTITLE eq EMI Service" /F
taskkill /FI "WINDOWTITLE eq Payment Service" /F
taskkill /FI "WINDOWTITLE eq Notification Service" /F

echo.
echo All services stopped (if they were running).
echo Note: If windows are still open, you may need to close them manually or use 'taskkill /F /IM java.exe' (Caution: Kills ALL Java apps).
pause
