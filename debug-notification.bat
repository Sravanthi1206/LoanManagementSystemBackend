@echo off
echo ==============================================
echo   Debugging Notification Service
echo ==============================================

REM Load .env
if exist .env (
    echo [Loading] .env variables...
    for /f "usebackq tokens=1,2 delims==" %%a in (".env") do (
        if not "%%a"=="" if not "%%a:~0,1%"=="#" (
            set "%%a=%%b"
        )
    )
)

echo.
echo [Checking Env Vars]
echo Mail Host: %SPRING_MAIL_HOST%
echo Mail Port: %SPRING_MAIL_PORT%
echo Mail User: %SPRING_MAIL_USERNAME%
echo Mail Pass: (Hidden)

echo.
echo [Starting Service]
java -jar notification-service/target/notification-service-0.0.1-SNAPSHOT.jar
pause
