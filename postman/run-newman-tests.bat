@echo off
REM Newman Test Runner for LMS APIs
REM Generates HTML reports for API testing

echo ===============================================
echo   LMS API Tests - Newman Runner
echo ===============================================
echo.

REM Check if Newman is installed
where newman >nul 2>&1
if errorlevel 1 (
    echo Newman is not installed. Installing...
    npm install -g newman newman-reporter-htmlextra
    if errorlevel 1 (
        echo Failed to install Newman. Please install manually:
        echo   npm install -g newman newman-reporter-htmlextra
        exit /b 1
    )
)

echo [Running] LMS API Tests...
echo.

REM Create reports directory
if not exist reports mkdir reports

REM Run Newman with HTML report
newman run LMS_API_Collection.postman_collection.json ^
    -e LMS_Local.postman_environment.json ^
    --reporters cli,htmlextra ^
    --reporter-htmlextra-export reports/LMS_API_Test_Report.html ^
    --reporter-htmlextra-title "LMS API Test Report" ^
    --reporter-htmlextra-browserTitle "LMS Tests" ^
    --reporter-htmlextra-showEnvironmentData ^
    --reporter-htmlextra-skipSensitiveData

echo.
echo ===============================================
echo   Test Complete!
echo ===============================================
echo.
echo Report generated: postman\reports\LMS_API_Test_Report.html
echo.
echo Open the report in your browser to view results.
pause
