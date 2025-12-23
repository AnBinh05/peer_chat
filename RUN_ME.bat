@echo off
title PeerTalk - P2P Chat Application
color 0A

echo.
echo ============================================================
echo           PEERTALK - P2P CHAT APPLICATION
echo ============================================================
echo.
echo IMPORTANT: This script uses Maven to run the application
echo            which automatically loads JavaFX modules.
echo.
echo ============================================================
echo.

REM Check if Maven is installed
where mvn >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Maven is not installed or not in PATH!
    echo Please install Maven and add it to PATH.
    pause
    exit /b 1
)

REM Check if Java is installed
where java >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Java is not installed or not in PATH!
    echo Please install Java 11 or higher.
    pause
    exit /b 1
)

echo [INFO] Checking Java version...
java -version
echo.

echo [INFO] Checking Maven version...
mvn --version
echo.

echo ============================================================
echo [STEP 1/2] Building project...
echo ============================================================
call mvn clean compile
if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Build failed! Please check the errors above.
    pause
    exit /b 1
)

echo.
echo ============================================================
echo [STEP 2/2] Starting application with JavaFX...
echo ============================================================
echo.
echo NOTE: Using 'mvn javafx:run' ensures JavaFX modules are loaded.
echo       DO NOT use 'java -jar' or 'java com.p2p.P2PApplication'
echo.
echo ============================================================
echo.

call mvn javafx:run

if %errorlevel% neq 0 (
    echo.
    echo ============================================================
    echo [ERROR] Application failed to start!
    echo ============================================================
    echo.
    echo Troubleshooting:
    echo 1. Make sure MySQL is running
    echo 2. Make sure database 'peertalk' exists (run database_setup.sql)
    echo 3. Check database configuration in src/main/java/com/p2p/db/Database.java
    echo 4. Make sure you're using 'mvn javafx:run' not 'java -jar'
    echo.
    echo For more help, see HOW_TO_RUN.md
    echo.
    pause
) else (
    echo.
    echo ============================================================
    echo Application closed.
    echo ============================================================
)


