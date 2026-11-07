@echo off
echo ========================================================
echo   Launching Apex Digital Banking System (CLI Console)
echo ========================================================

REM Navigate to directory of the script
cd /d "%~dp0"

REM Compile sources into bin/
if not exist "bin" mkdir bin
javac -cp "lib/*" -d bin src/model/*.java src/util/*.java src/dao/*.java src/ui/*.java src/*.java

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed. Please check Java errors above.
    pause
    exit /b %ERRORLEVEL%
)

REM Run Banking CLI application
java -cp "bin;lib/*" Projects_java.Banking_System.BankingApp
pause

