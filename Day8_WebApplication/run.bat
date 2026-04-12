@echo off
REM Faculty Database Web Application - Build and Run Script (Windows)
REM Usage: run.bat

echo.
echo ============================================
echo Faculty Database Web Application
echo ============================================
echo.

REM Check if Java is installed
where javac >nul 2>nul
if errorlevel 1 (
    echo X Java compiler (javac) is not installed
    echo Please install Java Development Kit (JDK)
    pause
    exit /b 1
)

echo. Java compiler found
echo.

REM Create output directories
if not exist "backend\bin" mkdir "backend\bin"
if not exist "backend\lib" mkdir "backend\lib"

echo. Compiling Java backend...

REM Compile Java files
cd backend

REM GSON library path
set GSON_PATH=lib\gson-2.10.1.jar

if exist "%GSON_PATH%" (
    set CLASSPATH=%GSON_PATH%;.
) else (
    echo WARNING: GSON library not found at %GSON_PATH%
    echo Please download from: https://github.com/google/gson/releases
    echo Save it as: backend\lib\gson-2.10.1.jar
    set CLASSPATH=.
)

javac -d bin -cp "%CLASSPATH%" src\com\faculty\*.java
if errorlevel 1 (
    echo X Compilation failed
    pause
    exit /b 1
)

echo. Compilation successful!
echo.

echo. Starting server on http://localhost:8080
echo.
echo Login credentials:
echo   Admin:  admin / admin123
echo   Faculty: prof1 / prof123
echo.
echo Press Ctrl+C to stop the server
echo.

REM Run the server
cd ..
java -cp "backend\bin;backend\lib\gson-2.10.1.jar" com.faculty.Server

pause
