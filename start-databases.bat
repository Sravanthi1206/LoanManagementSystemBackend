@echo off
REM Quick start for databases only
echo Starting LMS databases...

docker-compose up -d mysql mongodb mongo-express

echo.
echo Waiting for databases to be ready...
timeout /t 20 /nobreak >nul

echo.
echo Databases are ready!
echo   - MySQL:        localhost:3306
echo   - MongoDB:      localhost:27017  
echo   - Mongo Express: http://localhost:8081
echo.
echo You can now start the backend services.
pause
