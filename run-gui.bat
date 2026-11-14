@echo off
echo ========================================================
echo   Launching Apex Digital Banking System (JavaFX)
echo ========================================================

REM Navigate to directory of the script
cd /d "%~dp0"

REM Compile sources into bin/
if not exist "bin" mkdir bin
javac --module-path "javafx-sdk\lib" --add-modules javafx.controls -cp "lib/*" -d bin src\model\*.java src\util\*.java src\dao\*.java src\ui\*.java src\*.java

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed. Please check Java errors above.
    pause
    exit /b %ERRORLEVEL%
)

REM Run JavaFX Banking GUI application
java --module-path "javafx-sdk\lib" --add-modules javafx.controls -cp "bin;lib/*" Projects_java.Banking_System.BankingGUI
pause
