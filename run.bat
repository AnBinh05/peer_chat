@echo off
echo ========================================
echo   PeerTalk - P2P Chat Application
echo ========================================
echo.

echo Checking Java version...
java -version
echo.

echo Checking Maven version...
mvn --version
echo.

echo [1/2] Building project...
call mvn clean compile
if %errorlevel% neq 0 (
    echo.
    echo ERROR: Build failed!
    pause
    exit /b 1
)

echo.
echo [2/2] Running application with JavaFX...
echo IMPORTANT: Using 'mvn javafx:run' to ensure JavaFX modules are loaded correctly
echo.
call mvn javafx:run

if %errorlevel% neq 0 (
    echo.
    echo ERROR: Application failed to start!
    echo.
    echo Troubleshooting:
    echo 1. Make sure MySQL is running
    echo 2. Make sure database 'peertalk' exists
    echo 3. Check database configuration in Database.java
    echo 4. Make sure you're using 'mvn javafx:run' not 'java -jar'
    echo.
    pause
)

