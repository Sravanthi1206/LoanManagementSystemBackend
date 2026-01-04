@echo off
echo Stopping all LMS Microservices...

taskkill /FI "WINDOWTITLE eq Config" /F
taskkill /FI "WINDOWTITLE eq Discovery" /F
taskkill /FI "WINDOWTITLE eq API" /F
taskkill /FI "WINDOWTITLE eq Identity" /F
taskkill /FI "WINDOWTITLE eq Loan" /F
taskkill /FI "WINDOWTITLE eq EMI" /F
taskkill /FI "WINDOWTITLE eq Payment" /F
taskkill /FI "WINDOWTITLE eq Notification" /F

echo.
echo All services stopped.
echo Note: If windows are still open, you may need to close them manually or use 'taskkill /F /IM java.exe' (Caution: Kills ALL Java apps).
pause
